@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

@OptIn(ExperimentalContracts::class)
class TripViewModel(
    private val repository: TripRepository,
    private val tripId: String,
    private val addPlanUseCase: AddPlanUseCase,
    private val timeFormatter: TimeFormatter,
) : ViewModel(), AddPlanItemActionHandler by addPlanUseCase {

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
        }

        data class FlightDepartureItem(
            override val id: String,
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
            override val id: String,
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
            override val id: String,
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
            override val id: String,
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

        data class EmptyAddPlanItem(
            override val id: String,
            override val timestamp: Time,
            val showDivider: Boolean,
        ) :
            Timeable, Identifiable, TripItem
    }

    private val eventsFromTrip = repository.findTripById(tripId).map {
        ViewState(
            items = genItems(it),
        )
    }.onEach { localState.value = it }
    private val localState = MutableStateFlow(ViewState(items = emptyList()))
    val viewState: StateFlow<ViewState> =
        merge(eventsFromTrip, localState)
            .combine(addPlanUseCase.items) { state, addPlanItems ->
                state.updateItems {
                    addPlanItems.forEach { (id, addPlanItem) ->
                        indexOfFirst { it is TripItem.Identifiable && it.id == id }.takeIf { it != -1 }
                            ?.let {
                                set(
                                    it,
                                    addPlanItem
                                )
                            }
                    }
                }
            }
            .asStateFlow(initialValue = ViewState(emptyList()))

    fun addButtonTapped(itemId: String) {
        val tapped =
            viewState.value.items.find { it is TripItem.Identifiable && it.id == itemId } as TripItem.EmptyAddPlanItem
        val index = viewState.value.items.indexOf(tapped)
        updateItems {
            removeAt(index)
            add(index, addPlanUseCase.createAddPlanItem(tapped.timestamp))
        }
    }

    fun addPlanTypeChanged(itemId: String, newType: AddPlanUseCase.AddPlanItem.Type) {
        val item =
            viewState.value.items.find { it is TripItem.Identifiable && it.id == itemId }
                ?: return
        val index = viewState.value.items.indexOf(item)
        updateItems {
            this[index] = addPlanUseCase.typeChanged(item as AddPlanUseCase.AddPlanItem, newType)
        }
    }

    fun save(itemId: String) {
        val item =
            viewState.value.items.find { it is AddPlanUseCase.AddPlanItem && it.id == itemId }
                ?: return
        val entity = addPlanUseCase.saveItem(item as AddPlanUseCase.AddPlanItem)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.addFlight(tripId, entity)
                is Lodging -> repository.addLodging(tripId, entity)
            }
        }
    }

    fun cancelEdit(itemId: String) = Unit

    private fun updateItems(updater: MutableList<TripItem>.() -> Unit) {
        localState.value = localState.value.updateItems(updater)
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
        pairs.forEachIndexed { index, (timestamp, event) ->
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
                        timeInMillis = timestamp.toMidnight().copy(month = 1).timeInMillis
                    ),
                    month = timestamp.monthString,
                    year = timestamp.year.toString(),
                )
                items.add(monthItem)
                currentMonth = month
            }
            val place = getPlace(items, event)
            val firstInPlace = place != currentPlace
            if (firstInPlace) {
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
            val firstInSection = firstInDay || firstInPlace
            items.add(item)
            val emptyAddPlanItemIndex = if (firstInSection) {
                previousEventItem?.let { items.indexOf(it) + 1 }
            } else if (index == pairs.lastIndex) {
                items.size + 1
            } else {
                null
            }
            val emptyAddPlanItemTimestamp =
                if (firstInSection) previousEventItem?.timestamp else item.timestamp
            if (emptyAddPlanItemIndex != null && emptyAddPlanItemTimestamp != null) {
                items.add(
                    emptyAddPlanItemIndex,
                    TripItem.EmptyAddPlanItem(
                        UUID.randomUUID().toString(),
                        emptyAddPlanItemTimestamp,
                        showDivider = !firstInPlace,
                    )
                )
            }
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
        dayOfMonthStart = start.dayOfMonthString,
        dayOfWeekStart = start.dayOfWeekString,
        dayOfMonthEnd = end.dayOfMonthString,
        dayOfWeekEnd = end.dayOfWeekString,
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
                    dayOfMonth = event.departure.dayOfMonthString,
                    dayOfWeek = event.departure.dayOfWeekString,
                    time = event.departure.timeString,
                    destination = event.airportTo.city.name,
                    airport = event.airportFrom.name,
                )
            } else {
                TripItem.FlightArrivalItem(
                    id = "",
                    timestamp = event.arrival,
                    showDate = showDate,
                    dayOfMonth = event.arrival.dayOfMonthString,
                    dayOfWeek = event.arrival.dayOfWeekString,
                    time = event.arrival.timeString,
                    airport = event.airportTo.name,
                )
            }
        } else if (event is Lodging) {
            if (!items.contains { it is TripItem.HotelCheckInItem && it.timestamp == event.checkIn }) {
                TripItem.HotelCheckInItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = event.checkIn,
                    showDate = showDate,
                    dayOfWeek = event.checkIn.dayOfWeekString,
                    dayOfMonth = event.checkIn.dayOfMonthString,
                    time = event.checkIn.timeString,
                    hotelName = event.name ?: "",
                    hotelAddress = event.address,
                )
            } else {
                TripItem.HotelCheckOutItem(
                    id = UUID.randomUUID().toString(),
                    timestamp = event.checkout,
                    showDate = showDate,
                    dayOfWeek = event.checkout.dayOfWeekString,
                    dayOfMonth = event.checkout.dayOfMonthString,
                    time = event.checkout.timeString,
                    hotelName = event.name ?: event.address,
                )
            }
        } else {
            error("event must be FlightSegment or Lodging")
        }
        return item
    }

    private fun getPlace(items: List<TripItem>, event: Any): Place {
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

    private fun Time.toMidnight(): Time =
        copy(hour = 0, minute = 0, second = 0)

    private val Time.dayOfMonthString: String
        get() = timeFormatter.dayOfMonthString(this)

    private val Time.dayAndMonthString: String
        get() = timeFormatter.dayAndMonthString(this)

    private val Time.dayOfWeekString: String
        get() = timeFormatter.dayOfWeekString(this)

    private val Time.timeString: String
        get() = timeFormatter.timeString(this)

    private val Time.monthString: String
        get() = timeFormatter.monthString(this)

}

fun TripViewModel(
    serviceLocator: ServiceLocator,
    tripId: String,
): TripViewModel {
    val timeFormatter = TimeFormatter()
    return TripViewModel(
        serviceLocator.tripRepository,
        tripId,
        AddPlanUseCase(
            AddFlightUseCase(AddFlightRepository(), timeFormatter),
            AddLodgingUseCase(timeFormatter)
        ),
        timeFormatter,
    )
}

private fun <T> List<T>.contains(predicate: (T) -> Boolean) = find(predicate) != null

private fun TripViewModel.ViewState.updateItems(updater: MutableList<TripViewModel.TripItem>.() -> Unit): TripViewModel.ViewState {
    return TripViewModel.ViewState(
        items = items.toMutableList().apply(updater).toList()
    )
}

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

    fun Time.isWithin24Hours(other: Time): Boolean {
        return abs(timeInMillis - other.timeInMillis) <= TimeUnit.DAYS.toMillis(1)
    }
}

