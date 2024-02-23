@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.data.TripEvent
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
            val showDivider: Boolean
        }

        data class FlightDepartureItem(
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            override val showDivider: Boolean,
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
            override val showDivider: Boolean,
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
            override val showDivider: Boolean,
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
            override val showDivider: Boolean,
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
        val events = trip.flights.flatMap { it.segments } + trip.lodgings
        val pairs = events.flatMap { event ->
            when (event) {
                is FlightSegment -> listOf(event.departure to event, event.arrival to event)
                is Lodging -> listOf(event.checkIn to event, event.checkout to event)
            }
        }.sortedBy { (time, event) -> EventComparable(time, event) }
        return pairs.foldIndexed(listOf<TripItem>()) { index, items, (time, event) ->
            val isLastItem = index == pairs.lastIndex
            val lastEventIndex = items.indexOfLast { it is TripItem.EventItem }
            val lastEvent = items.getOrNull(lastEventIndex)
            val lastTime = lastEvent?.timestamp?.takeIf { time.dateString != it.dateString }
            val eventPlace = event.getPlace(time)
            val existingPlaceIndex =
                items.indexOfLast { it is TripItem.PlaceItem && it.placeName == eventPlace.name }
            val existingPlace = items.getOrNull(existingPlaceIndex) as? TripItem.PlaceItem
            val addPlace =
                existingPlace == null && (!isLastItem || !(event is FlightSegment && event.arrival == time))
            val firstInMonth =
                !items.contains { it is TripItem.MonthItem && it.month == time.monthString }
            val firstInDay =
                !items.contains { it is TripItem.EventItem && it.timestamp.dateString == time.dateString }
            val placeForRemoval = items.find {
                it is TripItem.PlaceItem && it.placeName != eventPlace.name && it.isOrigin(items)
            }
            items.toMutableList().apply {
                existingPlace?.let {
                    set(
                        existingPlaceIndex, it.copy(dateEnd = time.dayAndMonthString)
                    )
                }
                if (firstInDay && lastEvent != null) {
                    val updatedLastEvent = when (lastEvent) {
                        is TripItem.FlightDepartureItem -> lastEvent.copy(showDivider = true)
                        is TripItem.FlightArrivalItem -> lastEvent.copy(showDivider = true)
                        is TripItem.HotelCheckInItem -> lastEvent.copy(showDivider = true)
                        is TripItem.HotelCheckOutItem -> lastEvent.copy(showDivider = true)
                        else -> lastEvent
                    }
                    set(lastEventIndex, updatedLastEvent)
                }
                lastTime?.let { add(genDateRangeItem(from = it, to = time)) }
                if (addPlace) {
                    add(
                        TripItem.PlaceItem(
                            timestamp = time,
                            placeName = eventPlace.name,
                            imageUrl = eventPlace.coverImage ?: "",
                            dateStart = time.dayAndMonthString,
                            dateEnd = time.dayAndMonthString,
                        )
                    )
                }
                if (firstInMonth) {
                    add(
                        TripItem.MonthItem(
                            timestamp = time,
                            month = time.monthString,
                            year = time.asCalendar()[Calendar.YEAR].toString(),
                        )
                    )
                }
                add(genItem(time, event, firstInDay))
                placeForRemoval?.let { remove(it) }
            }
        }
    }

    private fun TripItem.PlaceItem.isOrigin(items: List<TripItem>): Boolean {
        val isFirstPlace = this == items.first { it is TripItem.PlaceItem }
        val index = items.indexOf(this)
        if (index == -1) return false
        val eventsAfter = items.subList(
            items.indexOf(this),
            items.size,
        ).filterIsInstance<TripItem.EventItem>()
        val hasSingleDepartureAfter =
            eventsAfter.let { it.size == 1 && it.first() is TripItem.FlightDepartureItem }
        return isFirstPlace && hasSingleDepartureAfter
    }

    private fun genDateRangeItem(
        from: Time, to: Time
    ): TripItem.DateRangeItem {
        val start = (from + TimeUnit.DAYS.toMillis(1))
        val end = to.toMidnight() - TimeUnit.MINUTES.toMillis(1)
        return TripItem.DateRangeItem(
            timestamp = start,
            dayOfMonthStart = start.dayOfMonthString(),
            dayOfWeekStart = start.dayOfWeekString(),
            dayOfMonthEnd = end.dayOfMonthString(),
            dayOfWeekEnd = end.dayOfWeekString(),
        )
    }

    private fun genItem(
        timestamp: Time,
        event: TripEvent,
        showDate: Boolean,
    ): TripItem.EventItem {
        contract { returns() implies (event is FlightSegment || event is Lodging) }
        return when (event) {
            is FlightSegment -> {
                if (timestamp == event.departure) {
                    TripItem.FlightDepartureItem(
                        timestamp = event.departure,
                        showDate = showDate,
                        dayOfMonth = event.departure.dayOfMonthString(),
                        dayOfWeek = event.departure.dayOfWeekString(),
                        time = event.departure.timeString(),
                        destination = event.airportTo.city.name,
                        airport = event.airportFrom.name,
                        showDivider = false,
                    )
                } else {
                    TripItem.FlightArrivalItem(
                        timestamp = event.arrival,
                        showDate = showDate,
                        dayOfMonth = event.arrival.dayOfMonthString(),
                        dayOfWeek = event.arrival.dayOfWeekString(),
                        time = event.arrival.timeString(),
                        airport = event.airportTo.name,
                        showDivider = false,
                    )
                }
            }

            is Lodging -> {
                if (timestamp == event.checkIn) {
                    TripItem.HotelCheckInItem(
                        timestamp = event.checkIn,
                        showDate = showDate,
                        dayOfWeek = event.checkIn.dayOfWeekString(),
                        dayOfMonth = event.checkIn.dayOfMonthString(),
                        time = event.checkIn.timeString(),
                        hotelName = event.name ?: "",
                        hotelAddress = event.address,
                        showDivider = false,
                    )
                } else {
                    TripItem.HotelCheckOutItem(
                        timestamp = event.checkout,
                        showDate = showDate,
                        dayOfWeek = event.checkout.dayOfWeekString(),
                        dayOfMonth = event.checkout.dayOfMonthString(),
                        time = event.checkout.timeString(),
                        hotelName = event.name ?: event.address,
                        showDivider = false,
                    )
                }
            }
        }
    }
}

fun TripViewModel(
    serviceLocator: ServiceLocator,
    tripId: String,
) = TripViewModel(
    serviceLocator.tripRepository,
    tripId,
)

private fun TripEvent.getPlace(referenceTime: Time) = when (this) {
    is FlightSegment -> if (referenceTime == departure) {
        airportFrom.city
    } else {
        airportTo.city
    }

    is Lodging -> city
}

private val Time.dateString
    get() = "$year=$month-$dayOfMonth"

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

private fun <T> List<T>.contains(predicate: (T) -> Boolean) = find(predicate) != null

private class EventComparable(
    private val time: Time, private val event: Any
) : Comparable<EventComparable> {
    override fun compareTo(other: EventComparable): Int {
        if (time.dateString != other.time.dateString) {
            return time.compareTo(other.time)
        }
        val comparison = type.priority - other.type.priority
        return if (comparison != 0) {
            comparison
        } else {
            time.compareTo(other.time)
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
        UNKNOWN(0), CHECKOUT(0), DEPARTURE(1), ARRIVAL(1), CHECKIN(2),
    }

    override fun toString(): String {
        return (time to event).toString()
    }
}

