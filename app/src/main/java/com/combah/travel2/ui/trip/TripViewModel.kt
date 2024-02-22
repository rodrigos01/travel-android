@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Flight
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
            items = genItemsNew(it),
        )
    }.asStateFlow(initialValue = ViewState(emptyList()))

    private fun genItemsNew(trip: Trip): List<TripItem> {
        val maps =
            (trip.flights + trip.lodgings).fold(mapOf<Place, Map<String, List<TripItem.EventItem>>>()) { map, event ->
                when (event) {
                    is Flight -> {
                        event.segments.fold(map) { flightMap, segment ->
                            flightMap.toMutableMap().also { placeMap ->
                                placeMap[segment.airportFrom.city] =
                                    (placeMap[segment.airportFrom.city] ?: mapOf()).toMutableMap()
                                        .also {
                                            it[segment.departure.dateString] =
                                                (it[segment.departure.dateString]
                                                    ?: emptyList()).plus(
                                                    listOf(
                                                        createFlightDepartureItem(segment)
                                                    )
                                                )
                                        }
                                placeMap[segment.airportTo.city] =
                                    (placeMap[segment.airportTo.city] ?: mapOf()).toMutableMap()
                                        .also {
                                            it[segment.arrival.dateString] =
                                                (it[segment.arrival.dateString]
                                                    ?: emptyList()).plus(
                                                    listOf(
                                                        createFlightArrivalEvent(segment)
                                                    )
                                                )
                                        }
                            }
                        }
                    }

                    is Lodging -> {
                        map.toMutableMap().also { placeMap ->
                            placeMap[event.city] =
                                (placeMap[event.city] ?: mapOf()).toMutableMap().also {
                                    it[event.checkIn.dateString] =
                                        (it[event.checkIn.dateString] ?: emptyList()) + listOf(
                                            createLodgingCheckInItem(event)
                                        )
                                    it[event.checkout.dateString] =
                                        (it[event.checkout.dateString] ?: emptyList()) + listOf(
                                            createLodgingCheckOutItem(event)
                                        )
                                }
                        }
                    }

                    else -> map
                }
            }

        val items = maps.entries.flatMap { (place, dateToItems) ->
            val items = dateToItems.entries.flatMap { (_, items) ->
                items.mapIndexed { index, item ->
                    if (index == 0) {
                        when (item) {
                            is TripItem.FlightDepartureItem -> item.copy(showDate = true)
                            is TripItem.FlightArrivalItem -> item.copy(showDate = true)
                            is TripItem.HotelCheckInItem -> item.copy(showDate = true)
                            is TripItem.HotelCheckOutItem -> item.copy(showDate = true)
                        }
                    } else {
                        item
                    }
                }
            }
            val timestamp = items.first().timestamp
            listOf(
                TripItem.PlaceItem(
                    timestamp = timestamp,
                    placeName = place.name,
                    imageUrl = place.coverImage ?: "",
                    dateStart = timestamp.dayAndMonthString,
                    dateEnd = timestamp.dayAndMonthString,
                )
            ) + items
        }
        return items
    }

    private fun createLodgingCheckInItem(event: Lodging) =
        TripViewModel.TripItem.HotelCheckInItem(
            timestamp = event.checkIn,
            showDate = false,
            dayOfWeek = event.checkIn.dayOfWeekString(),
            dayOfMonth = event.checkIn.dayOfMonthString(),
            time = event.checkIn.timeString(),
            hotelName = event.name ?: "",
            hotelAddress = event.address,
            showDivider = false,
        )

    private fun createLodgingCheckOutItem(event: Lodging) =
        TripViewModel.TripItem.HotelCheckOutItem(
            timestamp = event.checkout,
            showDate = false,
            dayOfWeek = event.checkout.dayOfWeekString(),
            dayOfMonth = event.checkout.dayOfMonthString(),
            time = event.checkout.timeString(),
            hotelName = event.name ?: event.address,
            showDivider = false,
        )

    private fun createFlightArrivalEvent(event: FlightSegment) =
        TripItem.FlightArrivalItem(
            timestamp = event.arrival,
            showDate = false,
            dayOfMonth = event.arrival.dayOfMonthString(),
            dayOfWeek = event.arrival.dayOfWeekString(),
            time = event.arrival.timeString(),
            airport = event.airportTo.name,
            showDivider = false,
        )

    private fun createFlightDepartureItem(event: FlightSegment) =
        TripViewModel.TripItem.FlightDepartureItem(
            timestamp = event.departure,
            showDate = false,
            dayOfMonth = event.departure.dayOfMonthString(),
            dayOfWeek = event.departure.dayOfWeekString(),
            time = event.departure.timeString(),
            destination = event.airportTo.city.name,
            airport = event.airportFrom.name,
            showDivider = false,
        )

    private fun genItems(trip: Trip): List<TripViewModel.TripItem> {
        val items = mutableListOf<TripItem>()
        var currentDay: String? = null
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
            val previousEventItem = (items.lastOrNull() as? TripItem.EventItem)
            val day = timestamp.run { "$year-$month-$dayOfMonth" }
            val firstInDay = day != currentDay
            val item = genItem(items, event, showDate = firstInDay)
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
                    timestamp = timestamp.toMidnight().update(dayOfMonth = 1),
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
                if (firstInDay) {
                    previousEventItem?.let {
                        items[items.indexOf(it)] = when (it) {
                            is TripItem.FlightDepartureItem -> it.copy(showDivider = true)
                            is TripItem.FlightArrivalItem -> it.copy(showDivider = true)
                            is TripItem.HotelCheckInItem -> it.copy(showDivider = true)
                            is TripItem.HotelCheckOutItem -> it.copy(showDivider = true)
                        }
                    }
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

fun TripViewModel(
    serviceLocator: ServiceLocator,
    tripId: String,
) = TripViewModel(
    serviceLocator.tripRepository,
    tripId,
)

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
                showDivider = false,
            )
        } else {
            TripViewModel.TripItem.FlightArrivalItem(
                timestamp = event.arrival,
                showDate = showDate,
                dayOfMonth = event.arrival.dayOfMonthString(),
                dayOfWeek = event.arrival.dayOfWeekString(),
                time = event.arrival.timeString(),
                airport = event.airportTo.name,
                showDivider = false,
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
                showDivider = false,
            )
        } else {
            TripViewModel.TripItem.HotelCheckOutItem(
                timestamp = event.checkout,
                showDate = showDate,
                dayOfWeek = event.checkout.dayOfWeekString(),
                dayOfMonth = event.checkout.dayOfMonthString(),
                time = event.checkout.timeString(),
                hotelName = event.name ?: event.address,
                showDivider = false,
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

private fun Time.toMidnight(): Time = update(hour = 0, minute = 0, second = 0)

private fun Time.isWithin24Hours(other: Time): Boolean {
    return abs(timeInMillis - other.timeInMillis) <= TimeUnit.DAYS.toMillis(1)
}

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

    enum class EventType {
        CHECKOUT,
        CHECKIN,
        ARRIVAL,
        DEPARTURE,
        UNKNOWN,
    }

    override fun toString(): String {
        return (time to event).toString()
    }
}

