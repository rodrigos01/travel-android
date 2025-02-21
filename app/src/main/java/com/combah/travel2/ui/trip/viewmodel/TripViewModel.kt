@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.data.TripEvent
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.BaseAddPlanItemActionHandler
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.TripItemState
import com.combah.travel2.ui.triplist.composable.TripListDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalContracts::class)
class TripViewModel(
    private val repository: TripRepository,
    private val tripId: String,
    private val addPlanUseCase: AddPlanUseCase,
    private val timeFormatter: TimeFormatter,
    private val navController: NavController,
) : ViewModel(), BaseAddPlanItemActionHandler, AddPlanItemActionHandler by addPlanUseCase {

    data class ViewState(
        val title: String,
        val items: List<TripItemState>,
    )

    private val reversibleItems = mutableMapOf<String, TripItemState>()

    private var TripItemState.Identifiable.original: TripItemState?
        get() = reversibleItems[id]
        set(value) {
            value?.let { reversibleItems[id] = it } ?: reversibleItems.remove(id)
        }

    private val trip = repository.findTripById(tripId)
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
    private val eventsFromTrip = trip.filterNotNull().map {
        ViewState(
            title = it.name ?: "Untitled Trip",
            items = genItems(it),
        )
    }.onEach { localState.value = it }
    private val localState = MutableStateFlow(ViewState(title = "", items = emptyList()))
    val viewState: StateFlow<ViewState> =
        merge(eventsFromTrip, localState).combine(addPlanUseCase.items) { state, addPlanItems ->
            state.updateItems {
                replaceAll { item ->
                    (item as? TripItemState.Identifiable)?.id?.let { addPlanItems[it] } ?: item
                }
            }
        }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = localState.value)

    fun tripNameChanged(newName: String) {
        viewModelScope.launch {
            repository.updateName(tripId, newName)
        }
    }

    fun deleteTrip() {
        viewModelScope.launch {
            repository.deleteTrip(tripId)
            navController.navigate(TripListDestination.ROUTE)
        }
    }

    fun addButtonTapped(itemId: String) {
        val tapped =
            viewState.value.items.find { it is TripItemState.Identifiable && it.id == itemId }
        val index = viewState.value.items.indexOf(tapped)
        updateItems {
            val allowStartDateSelection =
                tapped is TripItemState.DateRangeItemState || tapped is TripItemState.InitialAddPlanItemState
            val addPlanItem = addPlanUseCase.createAddPlanItem(
                (tapped as TripItemState.Timeable).timestamp,
                dateSelectionEnabled = allowStartDateSelection,
            )
            if (tapped is TripItemState.Replaceable) {
                addPlanItem.original = tapped
                removeAt(index)
                add(index, addPlanItem)
            } else {
                add(index + 1, addPlanItem)
            }
        }
    }

    fun emptyDateRowTapped(itemId: String) {
        val tapped =
            viewState.value.items.find { it is TripItemState.Identifiable && it.id == itemId }
        val index = viewState.value.items.indexOf(tapped)
        updateItems {
            val addPlanItem = addPlanUseCase.createAddPlanItem(
                (tapped as TripItemState.Timeable).timestamp,
                dateSelectionEnabled = false,
            )
            addPlanItem.original = tapped
            removeAt(index)
            add(index, addPlanItem)
        }
    }

    fun itemTapped(itemId: String) {
        val item =
            viewState.value.items.filterIsInstance<TripItemState.EventItemState>()
                .find { it.id == itemId }
                ?: return
        val entity = when (item) {
            is TripItemState.FlightDepartureItemState -> trip.value?.flights?.first { flight ->
                flight.segments.any { it.departure == item.timestamp && it.airportFrom.name == item.airport }
            }

            is TripItemState.FlightArrivalItemState -> trip.value?.flights?.first { flight ->
                flight.segments.any { it.arrival == item.timestamp && it.airportTo.name == item.airport }
            }

            is TripItemState.HotelCheckInItemState -> trip.value?.lodgings?.first {
                it.checkIn == item.timestamp && (it.name ?: it.address) == item.hotelName
            }

            is TripItemState.HotelCheckOutItemState -> trip.value?.lodgings?.first {
                it.checkout == item.timestamp && (it.name ?: it.address) == item.hotelName
            }
        } ?: return
        val index = viewState.value.items.indexOf(item)
        val addPlanItem = addPlanUseCase.createAddPlanItem(entity)
        updateItems {
            addPlanItem.original = item
            removeAt(index)
            add(index, addPlanUseCase.createAddPlanItem(entity))
        }
    }

    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) {
        val item =
            viewState.value.items.find { it is TripItemState.Identifiable && it.id == itemId }
                ?: return
        val index = viewState.value.items.indexOf(item)
        updateItems {
            this[index] = addPlanUseCase.typeChanged(item as AddPlanItemState, newType)
        }
    }

    override fun save(itemId: String) {
        val item =
            viewState.value.items.find { it is AddPlanItemState && it.id == itemId }
                ?: return
        val entity = addPlanUseCase.saveItem(item as AddPlanItemState)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.saveFlight(tripId, entity)
                is Lodging -> repository.saveLodging(tripId, entity)
            }
        }
    }

    override fun cancelEdit(itemId: String) {
        val item =
            viewState.value.items.find { it is AddPlanItemState && it.id == itemId } as? AddPlanItemState
                ?: return
        val itemIndex = viewState.value.items.indexOf(item)
        addPlanUseCase.removeItem(item)
        updateItems {
            removeIf { it is TripItemState.Identifiable && item.id == it.id }
            item.original?.let { add(itemIndex, it) }
        }
    }

    override fun delete(type: AddPlanItemState.Type, itemId: String) {
        viewModelScope.launch {
            when (type) {
                AddPlanItemState.Type.Flight -> repository.deleteFlight(tripId, itemId)
                AddPlanItemState.Type.Lodging -> repository.deleteLodging(tripId, itemId)
            }
        }
    }

    private fun updateItems(updater: MutableList<TripItemState>.() -> Unit) {
        localState.value = localState.value.updateItems(updater)
    }

    private fun genItems(trip: Trip): List<TripItemState> {
        val events = trip.flights.flatMap { it.segments } + trip.lodgings
        val pairs = events.flatMap { event ->
            when (event) {
                is FlightSegment -> listOf(event.departure to event, event.arrival to event)
                is Lodging -> listOf(event.checkIn to event, event.checkout to event)
            }
        }.sortedBy { (time, event) -> EventComparable(time, event) }
        val items = pairs.flatMapIndexed { index, (time, event) ->
            val placeItem = genPlaceItem(index, pairs)
            val firstInMonth = pairs.subList(0, index)
                .lastOrNull { it.first.monthString == time.monthString } == null
            val firstInDay = pairs.subList(0, index)
                .lastOrNull { it.first.dateString == time.dateString } == null
            val dateRangeItem = pairs.getOrNull(index + 1)?.let { genDateRangeItem(time, it.first) }
            val place = event.getPlace(time)
            val lastInPlace =
                pairs.getOrNull(index + 1)?.isDeparture == false && pairs.subList(index, pairs.size)
                    .takeWhile { it.place == place }.size == 1
            val lastInSection =
                index == pairs.lastIndex || pairs[index + 1].first.dateString != time.dateString || lastInPlace
            mutableListOf<TripItemState>().apply {
                placeItem?.let { add(it) }
                if (firstInMonth) {
                    add(
                        TripItemState.MonthItemState(
                            timestamp = time,
                            month = time.monthString,
                            year = time.year.toString(),
                        )
                    )
                }
                add(genItem(time, event, showDate = firstInDay))
                if (dateRangeItem != null) {
                    add(dateRangeItem)
                } else if (lastInSection) {
                    add(genEmptyAddPlanItem(time, showDivider = !lastInPlace))
                }
            }
        }
        return if (items.isNotEmpty()) {
            items
        } else {
            listOf(
                TripItemState.InitialAddPlanItemState(
                    UUID.randomUUID().toString(),
                    Time.now(),
                )
            )
        }
    }

    private fun genPlaceItem(
        index: Int, pairs: List<Pair<Time, TripEvent>>
    ): TripItemState.PlaceItemState? {
        val (time, event) = pairs[index]

        val place = event.getPlace(time)

        // Exclude return to origin
        if (index == pairs.lastIndex && event is FlightSegment && event.arrival == time && place == pairs.originPlace) return null

        // Exclude if previous adjacent events had same place
        val eventsBefore = pairs.subList(0, index).takeLastWhile { it.place == place }
        if (eventsBefore.isNotEmpty()) return null

        val placeEntries = pairs.subList(index, pairs.size).takeWhile { it.place == place }
        val lastEntry = placeEntries.last()
        // Exclude if only event in place is a departure
        if (placeEntries.size == 1 && lastEntry.isDeparture) return null

        return TripItemState.PlaceItemState(
            timestamp = time,
            placeName = place.name,
            imageUrl = place.coverImage ?: "",
            dateStart = time.dayAndMonthString,
            dateEnd = lastEntry.first.dayAndMonthString,
        )
    }

    private val List<Pair<Time, TripEvent>>.originPlace: Place?
        get() {
            return first().takeIf { it.isDeparture }?.place
        }

    private val Pair<Time, TripEvent>.place: Place
        get() = second.getPlace(first)

    private val Pair<Time, TripEvent>.isDeparture: Boolean
        get() = (second as? FlightSegment)?.departure == first


    private fun genEmptyAddPlanItem(
        emptyAddPlanItemTimestamp: Time, showDivider: Boolean
    ) = TripItemState.EmptyAddPlanItemState(
        UUID.randomUUID().toString(),
        emptyAddPlanItemTimestamp,
        showDivider = showDivider,
    )

    private fun genDateRangeItem(
        from: Time, to: Time
    ): TripItemState? {
        val start = from + 1.days
        val end = to.toMidnight() - 1.minutes
        return if (end <= start) {
            null
        } else if (end - 1.days >= start) {
            TripItemState.DateRangeItemState(
                id = UUID.randomUUID().toString(),
                timestamp = from,
                dayOfMonthStart = start.dayOfMonthString,
                dayOfWeekStart = start.dayOfWeekString,
                dayOfMonthEnd = end.dayOfMonthString,
                dayOfWeekEnd = end.dayOfWeekString,
            )
        } else {
            TripItemState.EmptyDateItemState(
                id = UUID.randomUUID().toString(),
                timestamp = from,
                dayOfMonth = start.dayOfMonthString,
                dayOfWeek = start.dayOfWeekString,
            )
        }
    }

    private fun genItem(
        timestamp: Time,
        event: TripEvent,
        showDate: Boolean,
    ): TripItemState.EventItemState {
        contract { returns() implies (event is FlightSegment || event is Lodging) }
        return when (event) {
            is FlightSegment -> {
                if (timestamp == event.departure) {
                    TripItemState.FlightDepartureItemState(
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
                    TripItemState.FlightArrivalItemState(
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
                    TripItemState.HotelCheckInItemState(
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
                    TripItemState.HotelCheckOutItemState(
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
    navController: NavController,
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
        navController,
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

private fun TripViewModel.ViewState.updateItems(updater: MutableList<TripItemState>.() -> Unit): TripViewModel.ViewState {
    return copy(
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

