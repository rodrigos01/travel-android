package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.map
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    data class ViewState(
        val items: List<TripItem>,
    )

    sealed interface TripItem {

        val timestamp: Time

        data class MonthItem(override val timestamp: Time, val month: String, val year: String) :
            TripItem

        data class DateRangeItem(
            override val timestamp: Time,
            val dayOfMonthStart: String,
            val dayOfWeekStart: String,
            val dayOfMonthEnd: String,
            val dayOfWeekEnd: String,
        ) : TripItem

        data class PlaceItem(
            override val timestamp: Time,
            val placeName: String,
            val imageUrl: String,
            val dateStart: String,
            val dateEnd: String,
        ) : TripItem

        sealed interface EventItem : TripItem {
            val showDate: Boolean
            val dayOfMonth: String?
            val dayOfWeek: String?
            val time: String
            val title: String?
            val subtitle: String?
        }

        data class FlightDepartureItem(
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val destination: String,
            val airport: String
        ) : EventItem {
            override val title = destination
            override val subtitle = airport
        }

        data class FlightArrivalItem(
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val airport: String
        ) : EventItem {
            override val title = null
            override val subtitle = airport
        }

        data class HotelCheckInItem(
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val hotelName: String,
            val hotelAddress: String,
        ) : EventItem {
            override val title = null
            override val subtitle = hotelAddress
        }

        data class HotelCheckOutItem(
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val hotelName: String
        ) : EventItem {
            override val title = null
            override val subtitle = hotelName
        }
    }

    private val trip = repository.findTripById(tripId)

    val viewState = trip.map {
        ViewState(
            items = genItems(it),
        )
    }.asStateFlow(initialValue = ViewState(emptyList()))

    private fun genItems(trip: Trip): List<TripItem> {
        val flights = trip.flights.flatMap { flight ->
            flight.segments.flatMap {
                listOf(
                    TripItem.FlightDepartureItem(
                        timestamp = it.departure,
                        showDate = false,
                        dayOfMonth = it.departure.dayOfMonthString(),
                        dayOfWeek = it.departure.dayOfWeekString(),
                        time = it.departure.timeString(),
                        destination = it.airportTo.city.name,
                        airport = it.airportFrom.name,
                    ), TripItem.FlightArrivalItem(
                        timestamp = it.arrival,
                        showDate = false,
                        dayOfMonth = it.arrival.dayOfMonthString(),
                        dayOfWeek = it.arrival.dayOfWeekString(),
                        time = it.arrival.timeString(),
                        airport = it.airportTo.name,
                    )
                )
            }
        }
        val hotels = trip.lodgings.flatMap {
            listOf(
                TripItem.HotelCheckInItem(
                    timestamp = it.checkIn,
                    showDate = false,
                    dayOfWeek = it.checkIn.dayOfWeekString(),
                    dayOfMonth = it.checkIn.dayOfMonthString(),
                    time = it.checkIn.timeString(),
                    hotelName = it.name ?: "",
                    hotelAddress = it.address,
                ), TripItem.HotelCheckOutItem(
                    timestamp = it.checkout,
                    showDate = false,
                    dayOfWeek = it.checkout.dayOfWeekString(),
                    dayOfMonth = it.checkout.dayOfMonthString(),
                    time = it.checkout.timeString(),
                    hotelName = it.name ?: "",
                )
            )
        }
        val events = (flights + hotels).sortedBy { it.timestamp }
        val months = events.distinctBy { it.timestamp.monthString }.map {
            TripItem.MonthItem(
                timestamp = it.timestamp.copy(
                    timeInMillis = it.timestamp.toMidnight().asCalendar().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis
                ),
                month = it.timestamp.monthString,
                year = it.timestamp.asCalendar()[Calendar.YEAR].toString(),
            )
        }
        val emptyDateRanges = events.zipWithNext { previous, next ->
            val start = (previous.timestamp + TimeUnit.DAYS.toMillis(1))
            val end = next.timestamp.toMidnight() - TimeUnit.MINUTES.toMillis(1)
            if (end > start) {
                TripItem.DateRangeItem(
                    timestamp = start,
                    dayOfMonthStart = start.dayOfMonthString(),
                    dayOfWeekStart = start.dayOfWeekString(),
                    dayOfMonthEnd = end.dayOfMonthString(),
                    dayOfWeekEnd = end.dayOfWeekString(),
                )
            } else {
                null
            }
        }.filterNotNull().distinct()
        val places = (trip.lodgings.flatMap {
            listOf(
                it.city to it.checkIn - 1, it.city to it.checkout - 1
            )
        } + trip.flights.flatMap { flight ->
            flight.segments.flatMap {
                listOf(
                    it.airportTo.city to it.arrival - 1,
                    it.airportFrom.city to it.departure - 1
                )
            }
        }).fold(mutableMapOf<Place, List<Time>>()) { map, (place, time) ->
            map.also { it[place] = listOf(time) + (it[place] ?: emptyList()) }
        }.map { (place, times) -> place to (times.min() to times.max()) }
            .sortedBy { (_, times) -> times.first }
            .zipWithNextWithLast { (currentPlace, currentTimes), next ->
                val nextTimes = next?.second
                if (nextTimes != null && (currentTimes.second >= nextTimes.first || currentTimes.second == currentTimes.first)) {
                    currentPlace to (currentTimes.first to nextTimes.first)
                } else {
                    currentPlace to currentTimes
                }
            }
            .map { (place, times) ->
                val (checkIn, checkOut) = times
                TripItem.PlaceItem(
                    checkIn,
                    place.name,
                    place.coverImage ?: "",
                    checkIn.dayAndMonthString,
                    checkOut.dayAndMonthString,
                )
            }.toList()
        val allItems = (events + months + emptyDateRanges + places).sortedBy { it.timestamp }
        return allItems.mapIndexed { index, item ->
            val prev = allItems.getOrNull(index - 1)
            if (item is TripItem.EventItem && (prev !is TripItem.EventItem || prev.timestamp.midnightTime < item.timestamp.midnightTime)) {
                val newItem = when (item) {
                    is TripItem.FlightDepartureItem -> item.copy(showDate = true)
                    is TripItem.FlightArrivalItem -> item.copy(showDate = true)
                    is TripItem.HotelCheckInItem -> item.copy(showDate = true)
                    is TripItem.HotelCheckOutItem -> item.copy(showDate = true)
                }
                newItem
            } else {
                item
            }
        }
    }
}

private fun Time.asCalendar(): Calendar = Calendar.getInstance().also {
    it.timeInMillis = timeInMillis
    it.timeZone = timeZone
}

private fun Time.dayOfMonthString(): String = asCalendar().get(Calendar.DAY_OF_MONTH).toString()

private val Time.dayAndMonthString: String
    get() = SimpleDateFormat("MMM d", Locale.getDefault()).apply {
        timeZone = this@dayAndMonthString.timeZone
    }.format(Date(timeInMillis))

private fun Time.dayOfWeekString(): String =
    DateFormatSymbols.getInstance().weekdays[asCalendar().get(Calendar.DAY_OF_WEEK)]

private fun Time.timeString(): String {
    val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).apply {
        timeZone = this@timeString.timeZone
    }
    val date = Date(timeInMillis)
    return formatter.format(date)
}

private val Time.monthString: String
    get() = DateFormatSymbols.getInstance().months[asCalendar().get(Calendar.MONTH)]

private fun Time.toMidnight(): Time = copy(timeInMillis = asCalendar().apply {
    set(Calendar.HOUR, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
}.timeInMillis)

private val Time.midnightTime: Long
    get() = toMidnight().timeInMillis

private fun <T, R> List<T>.zipWithNextWithLast(zipper: (current: T, next: T?) -> R) =
    zip(subList(1, size) + listOf(null)).map { (current, next) -> zipper(current, next) }

