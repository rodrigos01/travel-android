@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

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
            val hotelName: String,
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
        val items = mutableListOf<TripItem>()
        var currentDay: Time? = null
        var currentMonth: String? = null
        var currentPlace: Place? = null
        var currentPlaceItem: TripItem.PlaceItem? = null
        var lastTimestamp: Time? = null
        val pairs = (trip.lodgings.flatMap {
            listOf(
                it.checkIn to it, it.checkout to it
            )
        } + trip.flights.flatMap { it.segments }
            .flatMap {
                listOf(
                    it.departure to it,
                    it.arrival to it
                )
            }).sortedBy { (time, event) -> EventComparable(time, event) }
        pairs.forEach { (timestamp, event) ->
            val day = timestamp.toMidnight()
            val item = genItem(items, event, showDate = day != currentDay)
            currentDay = day
            lastTimestamp?.let {
                val start = (it + TimeUnit.DAYS.toMillis(1))
                val end = timestamp.toMidnight() - TimeUnit.MINUTES.toMillis(1)
                if (end > start) {
                    items.add(genDateRangeItem(start, end))
                }
            }
            lastTimestamp = timestamp
            val month = timestamp.monthString
            if (month != currentMonth) {
                val monthItem = TripItem.MonthItem(
                    timestamp = timestamp.copy(
                        timeInMillis = timestamp.toMidnight().asCalendar().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.timeInMillis
                    ),
                    month = timestamp.monthString,
                    year = timestamp.asCalendar()[Calendar.YEAR].toString(),
                )
                items.add(monthItem)
                currentMonth = month
            }
            val place = getPlace(items, event)
            if (place != currentPlace) {
                val lastPlaceItem = currentPlaceItem
                if (lastPlaceItem?.isOrigin(items) == true) {
                    items.remove(lastPlaceItem)
                }
                val placeItem = TripItem.PlaceItem(
                    timestamp = timestamp,
                    placeName = place.name,
                    imageUrl = place.coverImage ?: "",
                    dateStart = timestamp.dayAndMonthString,
                    dateEnd = timestamp.dayAndMonthString,
                )
                items.add(placeItem)
                currentPlaceItem = placeItem
                currentPlace = place
            } else {
                currentPlaceItem?.let {
                    val newPlaceItem = it.copy(dateEnd = timestamp.dayAndMonthString)
                    items[items.indexOf(it)] = newPlaceItem
                    currentPlaceItem = newPlaceItem
                }
            }
            items.add(item)
        }
        val lastPlaceItem = currentPlaceItem
        if (lastPlaceItem?.isOrigin(items) == true) {
            items.remove(lastPlaceItem)
        }
        return items
    }

    private fun TripItem.PlaceItem.isOrigin(items: List<TripItem>): Boolean {
        val isFirstPlace = this == items.first { it is TripItem.PlaceItem }
        val isLastPlace = this == items.last { it is TripItem.PlaceItem }
        val eventsAfter = items.subList(
            items.indexOf(this),
            items.size,
        )
        val hasSingleDepartureAfter = eventsAfter.filterIsInstance<TripItem.EventItem>()
            .run { size == 1 && first() is TripItem.FlightDepartureItem }
        val hasSingleArrivalAfter = eventsAfter.filterIsInstance<TripItem.EventItem>()
            .run { size == 1 && first() is TripItem.FlightArrivalItem }
        return (isFirstPlace && hasSingleDepartureAfter) || (isLastPlace && hasSingleArrivalAfter)
    }

    private fun genDateRangeItem(
        start: Time,
        end: Time
    ) = TripItem.DateRangeItem(
        timestamp = start,
        dayOfMonthStart = start.dayOfMonthString(),
        dayOfWeekStart = start.dayOfWeekString(),
        dayOfMonthEnd = end.dayOfMonthString(),
        dayOfWeekEnd = end.dayOfWeekString(),
    )
}

private fun genItem(
    items: List<TripViewModel.TripItem>,
    event: Any,
    showDate: Boolean,
): TripViewModel.TripItem.EventItem {
    contract { returns() implies (event is FlightSegment || event is Lodging) }
    val item = if (event is FlightSegment) {
        if (!items.contains { it is TripViewModel.TripItem.FlightDepartureItem && it.timestamp == event.departure }) {
            TripViewModel.TripItem.FlightDepartureItem(
                timestamp = event.departure,
                showDate = showDate,
                dayOfMonth = event.departure.dayOfMonthString(),
                dayOfWeek = event.departure.dayOfWeekString(),
                time = event.departure.timeString(),
                destination = event.airportTo.city.name,
                airport = event.airportFrom.name,
            )
        } else {
            TripViewModel.TripItem.FlightArrivalItem(
                timestamp = event.arrival,
                showDate = showDate,
                dayOfMonth = event.arrival.dayOfMonthString(),
                dayOfWeek = event.arrival.dayOfWeekString(),
                time = event.arrival.timeString(),
                airport = event.airportTo.name,
            )
        }
    } else if (event is Lodging) {
        if (!items.contains { it is TripViewModel.TripItem.HotelCheckInItem && it.timestamp == event.checkIn }) {
            TripViewModel.TripItem.HotelCheckInItem(
                timestamp = event.checkIn,
                showDate = showDate,
                dayOfWeek = event.checkIn.dayOfWeekString(),
                dayOfMonth = event.checkIn.dayOfMonthString(),
                time = event.checkIn.timeString(),
                hotelName = event.name ?: "",
                hotelAddress = event.address,
            )
        } else {
            TripViewModel.TripItem.HotelCheckOutItem(
                timestamp = event.checkout,
                showDate = showDate,
                dayOfWeek = event.checkout.dayOfWeekString(),
                dayOfMonth = event.checkout.dayOfMonthString(),
                time = event.checkout.timeString(),
                hotelName = event.name ?: event.address,
            )
        }
    } else {
        error("event must be FlightSegment or Lodging")
    }
    return item
}

fun getPlace(items: List<TripViewModel.TripItem>, event: Any): Place {
    return if (event is FlightSegment) {
        if (!items.contains { it is TripViewModel.TripItem.FlightDepartureItem && it.timestamp == event.departure }) {
            event.airportFrom.city
        } else {
            event.airportTo.city
        }
    } else if (event is Lodging) {
        event.city
    } else {
        error("event must be FlightSegment or Lodging")
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

private fun Time.isWithin24Hours(other: Time): Boolean {
    return abs(timeInMillis - other.timeInMillis) <= TimeUnit.DAYS.toMillis(1)
}

private fun Time.isSameDayIgnoringTimezone(other: Time): Boolean {
    return asCalendar().run {
        val otherCal = other.asCalendar()
        get(Calendar.DAY_OF_MONTH) == otherCal[Calendar.DAY_OF_MONTH] &&
                get(Calendar.MONTH) == otherCal[Calendar.MONTH] &&
                get(Calendar.YEAR) == otherCal[Calendar.YEAR]
    }
}

private fun <T, R> List<T>.zipWithNextWithLast(zipper: (current: T, next: T?) -> R) =
    zip(subList(1, size) + listOf(null)).map { (current, next) -> zipper(current, next) }

private fun <T> List<T>.contains(predicate: (T) -> Boolean) = find(predicate) != null

private class EventComparable(
    private val time: Time, private
    val event: Any
) :
    Comparable<EventComparable> {
    override fun compareTo(other: EventComparable): Int {
        val timeCompare = time.compareTo(other.time)
        if (!time.isWithin24Hours(other.time)) {
            return timeCompare
        }
        return when (type) {
            EventType.CHECKIN -> when (other.type) {
                EventType.CHECKOUT -> 1
                EventType.ARRIVAL,
                EventType.DEPARTURE -> if ((other.event as FlightSegment).airportTo.city == (event as Lodging).city) 1 else timeCompare

                else -> timeCompare
            }

            EventType.CHECKOUT -> when (other.type) {
                EventType.CHECKIN -> -1
                EventType.ARRIVAL,
                EventType.DEPARTURE -> if ((other.event as FlightSegment).airportFrom.city == (event as Lodging).city) 1 else timeCompare

                else -> timeCompare
            }

            EventType.DEPARTURE -> when (other.type) {
                EventType.ARRIVAL -> if (other.event == event) -1 else timeCompare
                EventType.CHECKIN -> if ((other.event as Lodging).city == (event as FlightSegment).airportTo.city) -1 else timeCompare
                EventType.CHECKOUT -> if ((other.event as Lodging).city == (event as FlightSegment).airportFrom.city) 1 else timeCompare
                else -> timeCompare
            }

            EventType.ARRIVAL -> when (other.type) {
                EventType.DEPARTURE -> if (other.event == event) 1 else timeCompare
                EventType.CHECKIN -> if ((other.event as Lodging).city == (event as FlightSegment).airportTo.city) -1 else timeCompare
                EventType.CHECKOUT -> if ((other.event as Lodging).city == (event as FlightSegment).airportFrom.city) 1 else timeCompare
                else -> timeCompare
            }

            else -> timeCompare
        }
    }

    val type: EventType
        get() {
            return when {
                event is Lodging && time == event.checkout -> EventType.CHECKOUT
                event is Lodging && time == event.checkIn -> EventType.CHECKIN
                event is FlightSegment && time == event.arrival -> EventType.ARRIVAL
                event is FlightSegment && time == event.departure -> EventType.DEPARTURE
                else -> EventType.UNKNOWN
            }
        }

    enum class EventType(val priority: Int) {
        CHECKOUT(0),
        CHECKIN(1),
        ARRIVAL(2),
        DEPARTURE(3),
        UNKNOWN(4),
    }

    override fun toString(): String {
        return (time to event).toString()
    }
}

