@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.data.TripEvent
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

        data class DateRangeItem(
            override val id: String,
            override val timestamp: Time,
            val dayOfMonthStart: String,
            val dayOfWeekStart: String,
            val dayOfMonthEnd: String,
            val dayOfWeekEnd: String,
        ) : TripItem, Timeable, Identifiable

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
        ) : Timeable, Identifiable, TripItem
    }

    private val eventsFromTrip = repository.findTripById(tripId).map {
        ViewState(
            items = genItems(it),
        )
    }.onEach { localState.value = it }
    private val addPlanItems = addPlanUseCase.items.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyMap(),
    )
    private val localState = MutableStateFlow(ViewState(items = emptyList()))
    val viewState: StateFlow<ViewState> =
        merge(eventsFromTrip, localState).combine(addPlanItems) { state, addPlanItems ->
            state.updateItems {
                addPlanItems.forEach { (id, addPlanItem) ->
                    indexOfFirst { it is TripItem.Identifiable && it.id == id }.takeIf { it != -1 }
                        ?.let {
                            set(
                                it, addPlanItem
                            )
                        }
                }
            }
        }.asStateFlow(initialValue = ViewState(emptyList()))

    fun addButtonTapped(itemId: String) {
        val tapped =
            viewState.value.items.find { it is TripItem.Identifiable && it.id == itemId }
        val index = viewState.value.items.indexOf(tapped)
        updateItems {
            val addPlanItem =
                addPlanUseCase.createAddPlanItem((tapped as TripItem.Timeable).timestamp)
            if (tapped is TripItem.EmptyAddPlanItem) {
                removeAt(index)
                add(index, addPlanItem)
            } else if (tapped is TripItem.DateRangeItem) {
                add(index + 1, addPlanItem)
            }
        }
    }

    fun addPlanTypeChanged(itemId: String, newType: AddPlanUseCase.AddPlanItem.Type) {
        val item =
            viewState.value.items.find { it is TripItem.Identifiable && it.id == itemId } ?: return
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

    fun cancelEdit(itemId: String) {
        val item =
            viewState.value.items.find { it is AddPlanUseCase.AddPlanItem && it.id == itemId } as? AddPlanUseCase.AddPlanItem
                ?: return
        addPlanUseCase.removeItem(item)
    }

    private fun updateItems(updater: MutableList<TripItem>.() -> Unit) {
        localState.value = localState.value.updateItems(updater)
    }

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
            val lastEvent = items.getOrNull(lastEventIndex) as? TripItem.EventItem
            val dateRangeItem = lastEvent?.timestamp?.let { genDateRangeItem(it, time) }
            val eventPlace = event.getPlace(time)
            val existingPlaceIndex =
                items.indexOfLast { it is TripItem.PlaceItem && it.placeName == eventPlace.name }
            val existingPlace = items.getOrNull(existingPlaceIndex) as? TripItem.PlaceItem
            val firstInPlace =
                existingPlace == null && (!isLastItem || !(event is FlightSegment && event.arrival == time))
            val firstInMonth =
                !items.contains { it is TripItem.MonthItem && it.month == time.monthString }
            val firstInDay =
                !items.contains { it is TripItem.EventItem && it.timestamp.dateString == time.dateString }
            val firstInSection = firstInDay || firstInPlace
            val emptyAddPlanItemIndex = if (firstInSection && dateRangeItem == null) {
                lastEvent?.let { items.indexOf(it) + 1 }
            } else {
                null
            }
            val emptyAddPlanItemTimestamp =
                if (firstInSection && lastEvent != null) lastEvent.timestamp else time
            val placeForRemoval = items.find {
                it is TripItem.PlaceItem && it.placeName != eventPlace.name && it.isOrigin(items)
            }
            items.toMutableList().apply {
                existingPlace?.let {
                    set(
                        existingPlaceIndex, it.copy(dateEnd = time.dayAndMonthString)
                    )
                }
                emptyAddPlanItemIndex?.let {
                    add(
                        it,
                        genEmptyAddPlanItem(emptyAddPlanItemTimestamp, showDivider = !firstInPlace)
                    )
                }
                dateRangeItem?.let { add(it) }
                if (firstInPlace) {
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
                            year = time.year.toString(),
                        )
                    )
                }
                add(genItem(time, event, firstInDay))
                placeForRemoval?.let { remove(it) }
                if (isLastItem) {
                    add(genEmptyAddPlanItem(emptyAddPlanItemTimestamp, showDivider = false))
                }
            }
        }
    }

    private fun genEmptyAddPlanItem(
        emptyAddPlanItemTimestamp: Time, showDivider: Boolean
    ) = TripItem.EmptyAddPlanItem(
        UUID.randomUUID().toString(),
        emptyAddPlanItemTimestamp,
        showDivider = showDivider,
    )

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
    ): TripItem.DateRangeItem? {
        val start = (from + TimeUnit.DAYS.toMillis(1))
        val end = to.toMidnight() - TimeUnit.MINUTES.toMillis(1)
        if (end <= start) {
            return null
        }
        return TripItem.DateRangeItem(
            id = UUID.randomUUID().toString(),
            timestamp = start,
            dayOfMonthStart = start.dayOfMonthString,
            dayOfWeekStart = start.dayOfWeekString,
            dayOfMonthEnd = end.dayOfMonthString,
            dayOfWeekEnd = end.dayOfWeekString,
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
                        id = UUID.randomUUID().toString(),
                        timestamp = event.arrival,
                        showDate = showDate,
                        dayOfMonth = event.arrival.dayOfMonthString,
                        dayOfWeek = event.arrival.dayOfWeekString,
                        time = event.arrival.timeString,
                        airport = event.airportTo.name,
                    )
                }
            }

            is Lodging -> {
                if (timestamp == event.checkIn) {
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
            }
        }
    }

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
            AddLodgingUseCase(AddLodgingRepository(), timeFormatter)
        ),
        timeFormatter,
    )
}

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

private fun <T> List<T>.contains(predicate: (T) -> Boolean) = find(predicate) != null

private fun TripViewModel.ViewState.updateItems(updater: MutableList<TripViewModel.TripItem>.() -> Unit): TripViewModel.ViewState {
    return TripViewModel.ViewState(
        items = items.toMutableList().apply(updater).toList()
    )
}

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

