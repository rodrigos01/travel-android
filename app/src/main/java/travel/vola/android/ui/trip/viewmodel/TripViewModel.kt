@file:OptIn(ExperimentalContracts::class)

package travel.vola.android.ui.trip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
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
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.dayAndMonthString
import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.minus
import travel.vola.android.extensions.monthString
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.timeString
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Identifiable
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Mapeable
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripEntity
import travel.vola.android.model.data.TripEvent
import travel.vola.android.model.data.WithCity
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private const val ADDING_PLAN_STATE_ID = "adding"

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

    data class ViewState(
        val title: String,
        val items: List<TripItemState>,
        val places: List<PlaceState>,
        val addPlanItemState: AddPlanItemState? = null,
        val focusedItemId: String? = null,
    )

    data class PlaceState(
        internal val place: Place,
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
    private val eventsFromTrip =
        trip.filterNotNull().map { currentTrip ->
            val items = genItems(currentTrip)
            val places =
                (currentTrip.lodgings + currentTrip.places + currentTrip.restaurants).fold(mapOf<Place, PlaceState>()) { map, entity: Mapeable ->
                    val current = map.getOrDefault(
                        entity.city, PlaceState(
                            place = entity.city,
                            listIndex = items.indexOfFirst { it is TripItemState.PlaceItemState && entity.city.name == it.placeName },
                            markers = emptyList()
                        )
                    )
                    map.toMutableMap().apply {
                        set(
                            entity.city, current.copy(
                                markers = current.markers + when (entity) {
                                    is Lodging -> MarkerViewState(
                                        position = Pair(entity.latitude, entity.longitude),
                                        name = entity.name ?: entity.address,
                                        type = MarkerType.Lodging,
                                    )

                                    is TimedPlace -> MarkerViewState(
                                        position = Pair(
                                            entity.place.latitude,
                                            entity.place.longitude
                                        ),
                                        name = entity.place.name,
                                        type = if (entity.place != entity.city) MarkerType.Place else MarkerType.City,
                                    )

                                    is RestaurantReservation -> MarkerViewState(
                                        position = Pair(
                                            entity.place.latitude,
                                            entity.place.longitude
                                        ),
                                        name = entity.place.name,
                                        type = MarkerType.Restaurant,
                                    )
                                }
                            )
                        )
                    }
                }
            ViewState(
                title = currentTrip.name ?: "Untitled Trip",
                items = items,
                places = places.values.toList(),
            )
        }
    private val addPlanItemsState = addPlanUseCase.items.onEach { state ->
        reversibleItems.keys.forEach { itemId ->
            if (!state.containsKey(itemId)) {
                reversibleItems.remove(itemId)
            }
        }
    }

    private data class ScrollState(val focusedIndex: Int, val firstVisibleIndex: Int)

    private val scrollState = MutableStateFlow(ScrollState(0, 0))
    val viewState: StateFlow<ViewState> = combine(
        eventsFromTrip,
        addPlanItemsState,
        scrollState
    ) { state, addPlanItems, currentScrollState ->
        val items = state.items.mapIndexed { index, item ->
            if (item is TripItemState.Replaceable) {
                addPlanItems[item.id]?.let { newItem ->
                    newItem.also { it.original = item }
                } ?: item
            } else {
                item
            }
        }
        val focusedDate =
            (state.items.getOrNull(currentScrollState.focusedIndex) as? TripItemState.Timeable)?.timestamp?.toLocalDate()
        val focusedDateItem = items.filterIsInstance<TripItemState.Focusable>().lastOrNull {
            items.indexOf(it)
                .let { index -> index >= currentScrollState.firstVisibleIndex && index <= currentScrollState.focusedIndex }
                    && it.timestamp.toLocalDate() == focusedDate && it.showDate
        }
        state.copy(
            items = items,
            addPlanItemState = addPlanItems[ADDING_PLAN_STATE_ID],
            focusedItemId = focusedDateItem?.id
        )
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
            navController.popBackStack()
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
        if (reversibleItems.containsKey(itemId)) {
            return
        }
        val item = viewState.value.items.filterIsInstance<TripItemState.Editable>()
            .find { it.id == itemId } ?: return
        val entity = item.entity
        if (entity != null) {
            addPlanUseCase.createAddPlanItem(itemId, entity)
        } else if (item is TripItemState.PlaceItemState) {
            val placeId = item.id.split("_").first()
            viewState.value.places.firstOrNull {
                it.place.id == placeId
            }?.place?.let { place ->
                addPlanUseCase.createAddPlanItem(
                    itemId,
                    TimedPlace(
                        id = placeId,
                        startDateTime = item.timestamp.toMidnight(),
                        hasStartTime = false,
                        city = place,
                        place = place,
                        endDateTime = null,
                        hasEndTime = false,
                    ),
                    deleteEnabled = false,
                )
            }
        }
    }

    fun onAddPlanTypeSelected(type: AddPlanItemState.Type?) {
        addPlanUseCase.removeItem(ADDING_PLAN_STATE_ID)
        val currentFocusedIndex = scrollState.value.focusedIndex
        val focusedItem = if (currentFocusedIndex == -1) {
            viewState.value.items.firstOrNull()
        } else {
            viewState.value.items.getOrNull(currentFocusedIndex)
        } ?: viewState.value.items.lastOrNull()
        val focusedDate = (focusedItem as? TripItemState.Timeable)?.timestamp
        if (type != null) {
            addPlanUseCase.createAddPlanItem(
                id = ADDING_PLAN_STATE_ID,
                focusedDate
                    ?: ZonedDateTime.now(),
                type = type
            )
        }
    }

    override fun save(itemId: String) {
        val lodgingSearchParams = addPlanUseCase.getLodgingSearchParams(tripId, itemId)
        if (lodgingSearchParams != null) {
            navController.navigate(route = lodgingSearchParams)
            return
        }
        val entity = addPlanUseCase.saveItem(itemId).let {
            if (itemId == ADDING_PLAN_STATE_ID) {
                it.copy(id = UUID.randomUUID().toString())
            } else {
                it
            }
        }
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.saveFlight(tripId, entity)
                is Lodging -> repository.saveLodging(tripId, entity)
                is TimedPlace -> repository.saveTimedPlace(tripId, entity)
                is RestaurantReservation -> repository.saveRestaurantReservation(tripId, entity)
            }
        }
    }

    private fun TripEntity.copy(id: String = this.id) = when (this) {
        is Flight -> copy(id = id)
        is Lodging -> copy(id = id)
        is TimedPlace -> copy(id = id)
        is RestaurantReservation -> copy(id = id)
    }

    override fun cancelEdit(itemId: String) {
        addPlanUseCase.removeItem(itemId) ?: return
    }

    override fun delete(type: AddPlanItemState.Type, itemId: String) {
        val entity = (reversibleItems[itemId] as? TripItemState.Editable)?.entity ?: return
        addPlanUseCase.removeItem(itemId)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.deleteFlight(tripId, entity.id)
                is Lodging -> repository.deleteLodging(tripId, entity.id)
                is TimedPlace -> repository.deleteTimedPlace(tripId, entity.id)
                is RestaurantReservation -> repository.deleteRestaurantReservation(
                    tripId,
                    entity.id
                )
            }
        }
    }

    private val TripItemState.Editable.entity: TripEntity?
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

            is TripItemState.PlaceItemState -> trip.value?.places?.firstOrNull { it.id == id }
            is TripItemState.RestaurantReservationItemState -> trip.value?.restaurants?.firstOrNull { it.id == id }
            is TripItemState.FlexibleDaySectionState -> null // TODO: find id from trip once its integrated
        }

    private fun genItems(trip: Trip): List<TripItemState> {
        val events =
            trip.flights.flatMap { it.segments } + trip.lodgings + trip.places + trip.restaurants
        val pairs = events.flatMap { event ->
            when (event) {
                is FlightSegment -> listOf(event.departure to event, event.arrival to event)
                is Lodging -> listOf(event.checkIn to event, event.checkout to event)
                is TimedPlace -> listOfNotNull(
                    event.startDateTime to event,
                    event.endDateTime?.let { it to event },
                )

                is RestaurantReservation -> listOf(event.dateTime to event)
                is FlexibleDaySection -> listOf(event.date to event)
            }
        }.sortedBy { (time, event) ->
            EventComparable(
                time, event
            )
        }
        val items = pairs.flatMapIndexed { index, (time, event) ->
            // Skip end of TimedPlace events
            if (event is TimedPlace && event.isTimedPlaceEnd(time)) {
                return@flatMapIndexed emptyList()
            }
            val placeItem = genPlaceItem(index, pairs)
            val previousItems = pairs.subList(0, index)
                .filterNot { (nextTime, nextEvent) -> nextEvent.isTimedPlaceEnd(nextTime) }
            val firstInMonth =
                previousItems.lastOrNull { it.first.monthString == time.monthString } == null
            val firstInDay =
                previousItems.lastOrNull { it.first.dateString == time.dateString } == null
            val place = if ((event as? TimedPlace)?.isDayTrip == true) {
                previousItems.lastOrNull()?.place
            } else {
                event.getPlace(time)
            }
            val firstInPlace = previousItems.lastOrNull()?.place != place
            val nextItems = pairs.nextItems(index)
            val nextItem = nextItems.firstOrNull()
            val lastInPlace =
                nextItems.takeWhile { it.place == place || (it.second as? TimedPlace)?.isDayTrip == true }
                    .isEmpty()
            val lastInDay = nextItem?.first?.dateString != time.dateString
            val firstInSection = firstInDay || firstInPlace
            val lastInSection = index == pairs.lastIndex || lastInDay || lastInPlace
            val dateRangeItem =
                nextItem?.let { genDateRangeItem(time, it.first, sectionId = place?.id ?: "") }
            mutableListOf<TripItemState>().apply {
                placeItem?.let { add(it) }
                if (firstInMonth) {
                    add(
                        TripItemState.MonthItemState(
                            timestamp = time,
                            month = time.monthString,
                            year = time.year.toString(),
                            sectionId = place?.id ?: ""
                        )
                    )
                }
                if (event !is TimedPlace || event.isDayTrip) {
                    val backgroundStyle =
                        if (firstInSection && lastInSection) {
                            TripItemState.EventItemState.BackgroundStyle.SINGLE
                        } else if (firstInSection) {
                            TripItemState.EventItemState.BackgroundStyle.TOP
                        } else if (lastInSection) {
                            TripItemState.EventItemState.BackgroundStyle.BOTTOM
                        } else {
                            TripItemState.EventItemState.BackgroundStyle.MIDDLE
                        }
                    add(
                        genItem(
                            time,
                            event,
                            showDate = firstInSection && !Pair(time, event).isReturn(pairs),
                            backgroundStyle = backgroundStyle,
                            sectionId = place?.id ?: "",
                        )
                    )
                }
                if (dateRangeItem != null) {
                    add(dateRangeItem)
                }
            }
        }
        return items.ifEmpty {
            listOf(
                TripItemState.InitialAddPlanItemState(
                    UUID.randomUUID().toString(),
                    ZonedDateTime.now(),
                )
            )
        }
    }

    private fun genPlaceItem(
        index: Int, pairs: List<Pair<ZonedDateTime, TripEvent>>,
    ): TripItemState.PlaceItemState? {
        val item = pairs[index]

        // Exclude return to origin
        if (item.isReturn(pairs)) return null

        val (time, event) = item

        val place = event.getPlace(time)


        // Exclude if previous adjacent events had same place or were day trips
        val eventsBefore =
            pairs.subList(0, index).filterNot { (it.second as? TimedPlace)?.isDayTrip == true }
                .takeLastWhile { it.place == place }
        if (eventsBefore.isNotEmpty()) return null

        val placeEntries = pairs.subList(index, pairs.size).takeWhile {
            it.place == place || (it.second as? TimedPlace)?.isDayTrip == true
        }
        val lastEntry = placeEntries.last()
        // Exclude if only event in place is a departure
        if (placeEntries.size == 1 && lastEntry.isDeparture) return null

        // Exclude if event is a day trip
        if (event is TimedPlace && event.isDayTrip) return null

        val dayAndMonth = time.dayAndMonthString
        val id =
            if (event is TimedPlace && event.city == event.place) event.id else "${place.id}_$dayAndMonth"

        return TripItemState.PlaceItemState(
            id = id,
            timestamp = time,
            placeName = place.name,
            imageUrl = place.coverImage ?: "",
            dateStart = dayAndMonth,
            dateEnd = lastEntry.first.dayAndMonthString,
            sectionId = place.id,
        )
    }

    private val List<Pair<ZonedDateTime, TripEvent>>.originPlace: Place?
        get() {
            return first().takeIf { it.isDeparture }?.place
        }

    private fun List<Pair<ZonedDateTime, TripEvent>>.nextItems(index: Int) = subList(
        (index + 1).coerceAtMost(lastIndex),
        size,
    ).filterNot { (nextTime, nextEvent) -> nextEvent.isTimedPlaceEnd(nextTime) }

    private fun TripEvent.isTimedPlaceEnd(
        referenceTime: ZonedDateTime,
    ) = this is TimedPlace && referenceTime == endDateTime && referenceTime != startDateTime

    private val Pair<ZonedDateTime, TripEvent>.place: Place
        get() = second.getPlace(first)

    private val Pair<ZonedDateTime, TripEvent>.isDeparture: Boolean
        get() = (second as? FlightSegment)?.departure == first

    private val TimedPlace.isDayTrip: Boolean
        get() = this.endDateTime == null || this.endDateTime.toMidnight() == this.startDateTime.toMidnight()

    private fun Pair<ZonedDateTime, TripEvent>.isReturn(pairs: List<Pair<ZonedDateTime, TripEvent>>): Boolean {
        val (time, event) = this
        return (this == pairs.last() && event is FlightSegment && event.arrival == time && event.getPlace(
            time
        ) == pairs.originPlace)
    }

    private fun genDateRangeItem(
        from: ZonedDateTime, to: ZonedDateTime, sectionId: String,
    ): TripItemState? {
        val start = from + 1.days
        val end = to.toMidnight() - 1.minutes
        return if (end <= start) {
            null
        } else if (end - 1.days >= start) {
            TripItemState.DateRangeItemState(
                id = UUID.randomUUID().toString(),
                timestamp = start,
                dayOfMonthStart = start.dayOfMonthString,
                dayOfWeekStart = start.dayOfWeekString,
                dayOfMonthEnd = end.dayOfMonthString,
                dayOfWeekEnd = end.dayOfWeekString,
                sectionId = sectionId,
            )
        } else {
            TripItemState.EmptyDateItemState(
                id = UUID.randomUUID().toString(),
                timestamp = start,
                dayOfMonth = start.dayOfMonthString,
                dayOfWeek = start.dayOfWeekString,
                sectionId = sectionId,
            )
        }
    }

    private fun genItem(
        timestamp: ZonedDateTime,
        event: TripEvent,
        showDate: Boolean,
        backgroundStyle: TripItemState.EventItemState.BackgroundStyle,
        sectionId: String,
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
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
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
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
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
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
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
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
                    )
                }
            }

            is TimedPlace -> TripItemState.TimedPlaceItemState(
                id = event.id,
                timestamp = event.startDateTime,
                showDate = showDate,
                dayOfMonth = event.startDateTime.dayOfMonthString,
                dayOfWeek = event.startDateTime.dayOfWeekString,
                time = event.startDateTime.timeString,
                showTime = event.hasStartTime,
                placeName = event.place.name,
                cityName = event.city.name,
                imageUrl = event.place.coverImage ?: "",
                backgroundStyle = backgroundStyle,
                sectionId = sectionId,
            )

            is RestaurantReservation -> TripItemState.RestaurantReservationItemState(
                id = event.id,
                timestamp = event.dateTime,
                showDate = showDate,
                dayOfMonth = event.dateTime.dayOfMonthString,
                dayOfWeek = event.dateTime.dayOfWeekString,
                time = event.dateTime.timeString,
                restaurantName = event.place.name,
                restaurantAddress = event.place.address,
                backgroundStyle = backgroundStyle,
                sectionId = sectionId,
            )

            is FlexibleDaySection -> TripItemState.FlexibleDaySectionState(
                id = event.name,
                timestamp = event.date,
                showDate = showDate,
                dayOfMonth = event.date.dayOfMonthString,
                dayOfWeek = event.date.dayOfWeekString,
                name = event.name,
                categories = event.categories.map { category ->
                    TripItemState.DaySectionCategory(
                        name = category.name,
                        items = category.items.map {
                            TripItemState.SectionOption(
                                id = it.id,
                                title = it.place.name,
                                subtitle = it.place.address,
                                imageUrl = it.place.coverImage ?: "",
                            )
                        },
                    )
                }
            )
        }
    }

    fun setScrollState(focusedIndex: Int, firstVisibleIndex: Int) {
        scrollState.value = ScrollState(focusedIndex, firstVisibleIndex)
    }

    override fun onCleared() {
        super.onCleared()
        useCaseScope.cancel()
    }

    class Factory(tripId: String) :
        ViewModelProvider.Factory by viewModelFactory(initializer = {
            TripViewModel(
                factoryDependencies.tripRepository,
                factoryDependencies.placeRepository,
                tripId,
                factoryDependencies.navController,
            )
        })

}

private fun TripEvent.getPlace(referenceTime: ZonedDateTime) = when (this) {
    is FlightSegment -> if (referenceTime == departure) {
        airportFrom.city
    } else {
        airportTo.city
    }

    is WithCity -> city
}

private class EventComparable(
    private val time: ZonedDateTime, private val event: TripEvent,
) : Comparable<EventComparable> {
    override fun compareTo(other: EventComparable): Int {
        if (time.dateString != other.time.dateString || type == EventType.UNKNOWN) {
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
                event is TimedPlace && time == event.endDateTime -> EventType.CHECKOUT
                event is Lodging && time == event.checkIn -> EventType.CHECKIN
                event is TimedPlace && time == event.startDateTime && event.endDateTime != null -> EventType.CHECKIN
                event is FlightSegment && time == event.arrival -> EventType.ARRIVAL
                event is FlightSegment && time == event.departure -> EventType.DEPARTURE
                event is TimedPlace && event.endDateTime == null -> EventType.PLACE
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
            EventType.CHECKOUT -> 3
            EventType.PLACE -> 4
            EventType.DEPARTURE -> 5
        }

    override fun toString(): String {
        return (time to event).toString()
    }
}

