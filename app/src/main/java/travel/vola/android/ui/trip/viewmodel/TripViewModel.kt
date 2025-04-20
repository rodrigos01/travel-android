@file:OptIn(ExperimentalContracts::class)

package travel.vola.android.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.createUseCaseScope
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.common.ui.state.MarkerViewState
import travel.vola.android.di.ServiceLocator
import travel.vola.android.extensions.dayAndMonthString
import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.minus
import travel.vola.android.extensions.monthString
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.timeString
import travel.vola.android.extensions.toMidnight
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Identifiable
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Time
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripEntity
import travel.vola.android.model.data.TripEvent
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.triplist.composable.TripListDestination
import java.util.UUID
import kotlin.collections.set
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalContracts::class)
class TripViewModel(
    private val repository: TripRepository,
    placeRepository: PlaceRepository,
    private val tripId: String,
    private val navController: NavController,
    private val useCaseScope: CoroutineScope = createUseCaseScope(),
    private val addPlanUseCase: AddPlanUseCase = AddPlanUseCase(
        placeRepository = placeRepository,
        coroutineScope = useCaseScope,
    ),
) : ViewModel(), AddPlanItemActionHandler by addPlanUseCase {

    constructor(
        serviceLocator: ServiceLocator,
        navController: NavController,
        tripId: String,
    ) : this(
        serviceLocator.tripRepository,
        serviceLocator.placeRepository,
        tripId,
        navController,
    )

    data class ViewState(
        val title: String,
        val items: List<TripItemState>,
        val places: List<PlaceState>,
    )

    data class PlaceState(
        val listIndex: Int,
        val markers: List<MarkerViewState>,
    )

    private val reversibleItems = mutableMapOf<String, TripItemState>()

    private var Identifiable.original: TripItemState?
        get() = reversibleItems[id]
        set(value) {
            value?.let { reversibleItems[id] = it } ?: reversibleItems.remove(id)
        }

    private val trip = repository.findTripById(tripId)
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
    private val eventsFromTrip = trip.filterNotNull().map { currentTrip ->
        val items = genItems(currentTrip)
        ViewState(
            title = currentTrip.name ?: "Untitled Trip",
            items = genItems(currentTrip),
            places = (currentTrip.lodgings.map { it.city })
                .map { place ->
                    PlaceState(
                        listIndex = items.indexOfFirst { it is TripItemState.PlaceItemState && place.name == it.placeName },
                        markers = listOf(
                            MarkerViewState(
                                position = Pair(place.latitude, place.longitude),
                                name = place.name,
                                type = MarkerType.City,
                            )
                        ),
                    )
                }
        )
    }
    private val addPlanItemsState = addPlanUseCase.items.onEach { state ->
        reversibleItems.keys.forEach { itemId ->
            if (!state.containsKey(itemId)) {
                reversibleItems.remove(itemId)
            }
        }
    }
    val viewState: StateFlow<ViewState> =
        eventsFromTrip.combine(addPlanItemsState) { state, addPlanItems ->
            val items = state.items.map { item ->
                if (item is TripItemState.Replaceable) {
                    addPlanItems[item.id]?.let { newItem ->
                        newItem.also { it.original = item }
                    } ?: item
                } else {
                    item
                }
            }
            state.copy(items = items)
        }.stateIn(
            viewModelScope, started = SharingStarted.Eagerly, initialValue = ViewState(
                title = "", items = emptyList(), places = emptyList()
            )
        )

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
        val tapped = viewState.value.items.find { it is Identifiable && it.id == itemId }
        val allowStartDateSelection =
            tapped is TripItemState.DateRangeItemState || tapped is TripItemState.InitialAddPlanItemState
        addPlanUseCase.createAddPlanItem(
            id = (tapped as? TripItemState.Replaceable)?.id,
            time = (tapped as TripItemState.Timeable).timestamp,
            dateSelectionEnabled = allowStartDateSelection,
        )
    }

    fun emptyDateRowTapped(itemId: String) {
        val tapped = viewState.value.items.find { it is Identifiable && it.id == itemId }
        addPlanUseCase.createAddPlanItem(
            id = (tapped as Identifiable).id,
            time = (tapped as TripItemState.Timeable).timestamp,
            dateSelectionEnabled = false,
        )
    }

    fun itemTapped(itemId: String) {
        val item = viewState.value.items.filterIsInstance<TripItemState.EventItemState>()
            .find { it.id == itemId } ?: return
        if (reversibleItems.containsKey(itemId)) {
            return
        }
        val entity = item.entity ?: return
        addPlanUseCase.createAddPlanItem(itemId, entity)
    }

    override fun save(itemId: String) {
        val lodgingSearchParams = addPlanUseCase.getLodgingSearchParams(tripId, itemId)
        if (lodgingSearchParams != null) {
            navController.navigate(route = lodgingSearchParams)
            return
        }
        val entity = addPlanUseCase.saveItem(itemId)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.saveFlight(tripId, entity)
                is Lodging -> repository.saveLodging(tripId, entity)
                is TimedPlace -> repository.saveTimedPlace(tripId, entity)
            }
        }
    }

    override fun cancelEdit(itemId: String) {
        addPlanUseCase.removeItem(itemId) ?: return
    }

    override fun delete(type: AddPlanItemState.Type, itemId: String) {
        val entity = (reversibleItems[itemId] as? TripItemState.EventItemState)?.entity ?: return
        addPlanUseCase.removeItem(itemId)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.deleteFlight(tripId, entity.id)
                is Lodging -> repository.deleteLodging(tripId, entity.id)
                is TimedPlace -> repository.deleteTimedPlace(tripId, entity.id)
            }
        }
    }

    private val TripItemState.EventItemState.entity: TripEntity?
        get() = when (this) {
            is TripItemState.FlightDepartureItemState -> trip.value?.flights?.first { flight ->
                flight.segments.any { it.departure == timestamp && it.airportFrom.name == airport }
            }

            is TripItemState.FlightArrivalItemState -> trip.value?.flights?.first { flight ->
                flight.segments.any { it.arrival == timestamp && it.airportTo.name == airport }
            }

            is TripItemState.HotelCheckInItemState -> trip.value?.lodgings?.first {
                it.checkIn == timestamp && (it.name ?: it.address) == hotelName
            }

            is TripItemState.HotelCheckOutItemState -> trip.value?.lodgings?.first {
                it.checkout == timestamp && (it.name ?: it.address) == hotelName
            }

            is TripItemState.TimedPlaceItemState -> trip.value?.places?.first {
                it.id == id
            }
        }

    private fun genItems(trip: Trip): List<TripItemState> {
        val events = trip.flights.flatMap { it.segments } + trip.lodgings + trip.places
        val pairs = events.flatMap { event ->
            when (event) {
                is FlightSegment -> listOf(event.departure to event, event.arrival to event)
                is Lodging -> listOf(event.checkIn to event, event.checkout to event)
                is TimedPlace -> listOf(event.time to event)
            }
        }.sortedBy { (time, event) ->
            EventComparable(
                time, event
            )
        }
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
                if (event !is TimedPlace || event.place != place) {
                    add(genItem(time, event, showDate = firstInDay))
                }
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

            is TimedPlace -> TripItemState.TimedPlaceItemState(
                id = event.id,
                timestamp = event.time,
                showDate = showDate,
                dayOfMonth = event.time.dayOfMonthString,
                dayOfWeek = event.time.dayOfWeekString,
                time = event.time.timeString,
                placeName = event.place.name,
                cityName = event.city.name,
                imageUrl = event.place.coverImage ?: "",
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        useCaseScope.cancel()
    }

}

private fun TripEvent.getPlace(referenceTime: Time) = when (this) {
    is FlightSegment -> if (referenceTime == departure) {
        airportFrom.city
    } else {
        airportTo.city
    }

    is Lodging -> city
    is TimedPlace -> city
}

private val Time.dateString
    get() = "$year=$month-$dayOfMonth"

private class EventComparable(
    private val time: Time, private val event: TripEvent
) : Comparable<EventComparable> {
    override fun compareTo(other: EventComparable): Int {
        if (time.dateString != other.time.dateString) {
            return time.compareTo(other.time)
        }
        val comparison = if (event.getPlace(time) == other.event.getPlace(other.time)) {
            type.priorityInPlace - other.type.priorityInPlace
        } else {
            type.priorityInDay - other.type.priorityInDay
        }
        return if (comparison != 0) {
            comparison
        } else {
            time.compareTo(other.time)
        }
    }

    enum class EventType {
        UNKNOWN, CHECKOUT, DEPARTURE, ARRIVAL, CHECKIN, PLACE
    }

    val type: EventType
        get() {
            return when {
                event is Lodging && time == event.checkout -> EventType.CHECKOUT
                event is Lodging && time == event.checkIn -> EventType.CHECKIN
                event is FlightSegment && time == event.arrival -> EventType.ARRIVAL
                event is FlightSegment && time == event.departure -> EventType.DEPARTURE
                event is TimedPlace -> EventType.PLACE
                else -> EventType.UNKNOWN
            }
        }

    val EventType.priorityInDay: Int
        get() = when (this) {
            EventType.UNKNOWN -> 0
            EventType.CHECKOUT -> 0
            EventType.DEPARTURE -> 1
            EventType.ARRIVAL -> 1
            EventType.CHECKIN -> 2
            EventType.PLACE -> 3
        }

    val EventType.priorityInPlace: Int
        get() = when (this) {
            EventType.UNKNOWN -> 0
            EventType.ARRIVAL -> 1
            EventType.CHECKIN -> 2
            EventType.CHECKOUT -> 4
            EventType.PLACE -> 3
            EventType.DEPARTURE -> 5
        }

    override fun toString(): String {
        return (time to event).toString()
    }
}

