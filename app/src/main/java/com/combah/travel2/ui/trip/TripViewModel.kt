@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

@OptIn(ExperimentalContracts::class)
class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    data class ViewState(
        val items: List<TripItem>,
    )

    sealed interface TripItem {

        interface Timeable {
            val timestamp: Time
        }

        data class MonthItem(override val timestamp: Time, val month: String, val year: String) :
            TripItem, Timeable

        data class DateRangeItem(
            override val timestamp: Time,
            val dayOfMonthStart: String,
            val dayOfWeekStart: String,
            val dayOfMonthEnd: String,
            val dayOfWeekEnd: String,
        ) : TripItem, Timeable

        data class PlaceItem(
            override val timestamp: Time,
            val placeName: String,
            val imageUrl: String,
            val dateStart: String,
            val dateEnd: String,
        ) : TripItem, Timeable

        interface Identifiable {
            val id: String
        }

        sealed interface EventItem : TripItem, Timeable, Identifiable {
            val showDate: Boolean
            val dayOfMonth: String?
            val dayOfWeek: String?
            val time: String
            val title: String?
            val subtitle: String?
            val showAddButton: Boolean
            val showDivider: Boolean
            fun withNewValues(
                showAddButton: Boolean = this.showAddButton,
                showDivider: Boolean = this.showDivider,
            ): EventItem
        }

        data class FlightDepartureItem(
            override val id: String,
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            override val showAddButton: Boolean,
            override val showDivider: Boolean,
            val destination: String,
            val airport: String
        ) : EventItem {
            override val title = destination
            override val subtitle = airport
            override fun withNewValues(
                showAddButton: Boolean,
                showDivider: Boolean
            ): FlightDepartureItem = copy(showAddButton = showAddButton, showDivider = showDivider)
        }

        data class FlightArrivalItem(
            override val id: String,
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            override val showAddButton: Boolean,
            override val showDivider: Boolean,
            val airport: String
        ) : EventItem {
            override val title = null
            override val subtitle = airport

            override fun withNewValues(
                showAddButton: Boolean,
                showDivider: Boolean
            ): FlightArrivalItem = copy(showAddButton = showAddButton, showDivider = showDivider)
        }

        data class HotelCheckInItem(
            override val id: String,
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            override val showAddButton: Boolean,
            override val showDivider: Boolean,
            val hotelName: String,
            val hotelAddress: String,
        ) : EventItem {
            override val title = null
            override val subtitle = hotelAddress

            override fun withNewValues(
                showAddButton: Boolean,
                showDivider: Boolean
            ): HotelCheckInItem = copy(showAddButton = showAddButton, showDivider = showDivider)
        }

        data class HotelCheckOutItem(
            override val id: String,
            override val timestamp: Time,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val showAddButton: Boolean,
            override val showDivider: Boolean,
            override val time: String,
            val hotelName: String,
        ) : EventItem {
            override val title = null
            override val subtitle = hotelName

            override fun withNewValues(
                showAddButton: Boolean,
                showDivider: Boolean
            ): HotelCheckOutItem = copy(showAddButton = showAddButton, showDivider = showDivider)
        }

        sealed class AddPlanItem : TripItem, Identifiable {
            val types: List<AddPlanType> = AddPlanType.entries
        }

        data class AddFlightItem(
            override val id: String,
            private val departure: Time? = null,
            private val airportFrom: Airport? = null,
            private val arrival: Time? = null,
            private val airportTo: Airport? = null,
        ) : AddPlanItem() {
            val departureTime: String?
                get() = departure?.timeString()
            val airportFromName: String?
                get() = airportFrom?.name

            val arrivalTime: String?
                get() = arrival?.timeString()

            val arrivalDayOfMonth: String?
                get() = arrival?.dayOfMonthString()
            val arrivalDayOfWeek: String?
                get() = arrival?.dayOfWeekString()
            val airportToName: String?
                get() = airportTo?.name
        }

        data class AddLodgingItem(
            override val id: String,
            private val lodging: Lodging? = null,
            private val checkIn: Time? = null,
            private val checkOut: Time? = null,
        ) : AddPlanItem() {
            val checkInTime: String?
                get() = checkIn?.timeString()

            val checkOutDayOfMonth: String?
                get() = checkOut?.dayOfMonthString()
            val checkOutDayOfWeek: String?
                get() = checkOut?.dayOfWeekString()
            val checkOutTime: String?
                get() = checkOut?.timeString()
        }
    }

    enum class AddPlanType {
        Flight,
        Lodging,
    }

    private val eventsFromTrip = repository.findTripById(tripId).map {
        ViewState(
            items = genItems(it),
        )
    }.onEach { localState.value = it }
    private val localState = MutableStateFlow(ViewState(items = emptyList()))
    val viewState: StateFlow<ViewState> =
        merge(eventsFromTrip, localState).asStateFlow(initialValue = ViewState(emptyList()))

    fun addButtonTapped(itemId: String) {
        val index = viewState.value.items.let { items ->
            items.indexOf(items.find { it is TripItem.Identifiable && it.id == itemId })
        }
        val newItems = viewState.value.items.toMutableList()
        newItems.add(index + 1, TripItem.AddFlightItem(id = UUID.randomUUID().toString()))
        localState.value = ViewState(items = newItems)
    }

    fun addPlanTypeChanged(itemId: String, newType: AddPlanType) {
        val item = viewState.value.items.find { it is TripItem.Identifiable && it.id == itemId }
        if (item is TripItem.AddFlightItem && newType == AddPlanType.Flight) {
            return
        }
        if (item is TripItem.AddLodgingItem && newType == AddPlanType.Lodging) {
            return
        }
        val index = viewState.value.items.indexOf(item)
        val newItems = viewState.value.items.toMutableList()
        newItems[index] = when (newType) {
            AddPlanType.Flight -> TripItem.AddFlightItem(id = UUID.randomUUID().toString())
            AddPlanType.Lodging -> TripItem.AddLodgingItem(id = UUID.randomUUID().toString())
        }
        localState.value = ViewState(items = newItems)
    }

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
            val previousEventItem = (items.lastOrNull() as? TripItem.EventItem)
            val day = timestamp.toMidnight()
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
                if (firstInDay) {
                    previousEventItem?.let {
                        items[items.indexOf(it)] = it.withNewValues(
                            showAddButton = true,
                            showDivider = true,
                        )
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

    fun TripViewModel(
    serviceLocator: ServiceLocator,
    tripId: String,
) = TripViewModel(
    serviceLocator.tripRepository,
    tripId,
)
    
private fun genItem(
        items: List<TripItem>,
        event: Any,
        showDate: Boolean,
    ): TripItem.EventItem {
        contract { returns() implies (event is FlightSegment || event is Lodging) }
        val item = if (event is FlightSegment) {
            if (!items.contains { it is TripItem.FlightDepartureItem && it.timestamp == event.departure }) {
                TripItem.FlightDepartureItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = event.departure,
                    showDate = showDate,
                    dayOfMonth = event.departure.dayOfMonthString(),
                    dayOfWeek = event.departure.dayOfWeekString(),
                    time = event.departure.timeString(),
                    destination = event.airportTo.city.name,
                    airport = event.airportFrom.name,
                    showAddButton = false,
                    showDivider = false,
                )
            } else {
                TripItem.FlightArrivalItem(
                    id = "",
                    timestamp = event.arrival,
                    showDate = showDate,
                    dayOfMonth = event.arrival.dayOfMonthString(),
                    dayOfWeek = event.arrival.dayOfWeekString(),
                    time = event.arrival.timeString(),
                    airport = event.airportTo.name,
                    showAddButton = false,
                    showDivider = false,
                )
            }
        } else if (event is Lodging) {
            if (!items.contains { it is TripItem.HotelCheckInItem && it.timestamp == event.checkIn }) {
                TripItem.HotelCheckInItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = event.checkIn,
                    showDate = showDate,
                    dayOfWeek = event.checkIn.dayOfWeekString(),
                    dayOfMonth = event.checkIn.dayOfMonthString(),
                    time = event.checkIn.timeString(),
                    hotelName = event.name ?: "",
                    hotelAddress = event.address,
                    showAddButton = false,
                    showDivider = false,
                )
            } else {
                TripItem.HotelCheckOutItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = event.checkout,
                    showDate = showDate,
                    dayOfWeek = event.checkout.dayOfWeekString(),
                    dayOfMonth = event.checkout.dayOfMonthString(),
                    time = event.checkout.timeString(),
                    hotelName = event.name ?: event.address,
                    showAddButton = false,
                    showDivider = false,
                )
            }
        } else {
            error("event must be FlightSegment or Lodging")
        }
        return item
    }

    fun getPlace(items: List<TripItem>, event: Any): Place {
        return if (event is FlightSegment) {
            if (!items.contains { it is TripItem.FlightDepartureItem && it.timestamp == event.departure }) {
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

