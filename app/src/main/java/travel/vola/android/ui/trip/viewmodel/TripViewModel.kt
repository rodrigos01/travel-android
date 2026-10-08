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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.createUseCaseScope
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.common.ui.state.MarkerViewState
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.asISO8601String
import travel.vola.android.extensions.getDestinations
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Identifiable
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Mapeable
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripEntity
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.assistant.composable.TripCreationAssistantDestination
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.type
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.contracts.ExperimentalContracts

private const val ADDING_PLAN_STATE_ID = "adding"

@OptIn(ExperimentalContracts::class)
class TripViewModel(
    private val repository: TripRepository,
    placeRepository: PlaceRepository,
    private val tripId: String,
    private val navController: NavController,
    private val useCaseScope: CoroutineScope = createUseCaseScope(),
    private val suggestionsUseCase: SuggestionsUseCase = SuggestionsUseCase(
        repository = GenAIRepository(),
    ),
    private val flexibleSectionUseCase: FlexibleSectionUseCase = FlexibleSectionUseCase(
        tripId = tripId,
        repository = repository,
        coroutineScope = useCaseScope,
        suggestionsUseCase = suggestionsUseCase,
    ),
    private val addPlanUseCase: AddPlanUseCase = AddPlanUseCase(
        placeRepository = placeRepository,
        coroutineScope = useCaseScope,
        flexibleSectionUseCase = flexibleSectionUseCase,
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

    private val suggestions = suggestionsUseCase.state
    private val flexibleSectionItems = flexibleSectionUseCase.flexibleSectionItems
    private val itineraryMapper = ItineraryMapper(
        createFlexibleSectionState = { section, showDate, backgroundStyle ->
            flexibleSectionUseCase.createState(
                section,
                showDate = showDate,
                backgroundStyle = backgroundStyle,
                isGenerated = true, // Flexible items not coming from the use case are generated
            )
        },
    )
    private val eventsFromTrip = combine(
        trip.filterNotNull(),
        flexibleSectionItems,
        suggestions,
    ) { currentTrip, sectionItems, suggestions ->
        val items = itineraryMapper.map(currentTrip.itinerary, sectionItems, suggestions)
        val places =
            (currentTrip.lodgings + currentTrip.places + currentTrip.restaurants + currentTrip.flexibleSections).fold(
                mapOf<Place, PlaceState>(),
            ) { map, entity: Mapeable ->
                val current = map.getOrDefault(
                    entity.city,
                    PlaceState(
                        place = entity.city,
                        listIndex = items.indexOfFirst { it is TripItemState.PlaceItemState && it.sectionId == entity.city.id },
                        markers = emptyList(),
                    ),
                )
                map.toMutableMap().apply {
                    set(
                        entity.city,
                        current.copy(
                            markers = current.markers + createMarkerStates(entity),
                        ),
                    )
                }
            }
        ViewState(
            title = currentTrip.name ?: "Untitled Trip",
            items = items,
            places = places.values.toList(),
        )
    }

    private fun createMarkerStates(entity: Mapeable): List<MarkerViewState> = when (entity) {
        is Lodging -> listOf(
            MarkerViewState(
                position = Pair(entity.latitude, entity.longitude),
                name = entity.name ?: entity.address,
                type = MarkerType.Lodging,
            ),
        )

        is TimedPlace -> listOf(
            MarkerViewState(
                position = Pair(
                    entity.place.latitude,
                    entity.place.longitude,
                ),
                name = entity.place.name,
                type = if (entity.place != entity.city) MarkerType.Place else MarkerType.City,
            ),
        )

        is RestaurantReservation -> listOf(
            MarkerViewState(
                position = Pair(
                    entity.place.latitude,
                    entity.place.longitude,
                ),
                name = entity.place.name,
                type = MarkerType.Restaurant,
            ),
        )

        is FlexibleDaySection -> entity.categories.flatMap { it.items }.map { item ->
            MarkerViewState(
                position = Pair(
                    item.place.latitude,
                    item.place.longitude,
                ),
                name = item.place.name,
                type = MarkerType.Place,
            )
        }
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

    private val itemsGeneratingSuggestions = MutableStateFlow<Set<String>>(emptySet())
    val viewState: StateFlow<ViewState> = combine(
        eventsFromTrip,
        addPlanItemsState,
        scrollState,
        itemsGeneratingSuggestions,
    ) { state, addPlanItems, currentScrollState, generating ->
        val items = state.items.mapIndexed { index, item ->
            if (item is Identifiable && generating.contains(item.id)) {
                when (item) {
                    is TripItemState.DateRangeItemState -> item.copy(isGeneratingPlans = true)
                    is TripItemState.EmptyDateItemState -> item.copy(isGeneratingPlans = true)
                    else -> item
                }
            } else if (item is TripItemState.Replaceable) {
                addPlanItems[item.id]?.let { newItem ->
                    newItem.also { it.original = item }
                } ?: item
            } else {
                item
            }
        }
        val focusedDate =
            state.items.getOrNull(currentScrollState.focusedIndex)?.timestamp?.toLocalDate()
        val focusedDateItem = items.filterIsInstance<TripItemState.Focusable>().lastOrNull {
            items.indexOf(it)
                .let { index -> index >= currentScrollState.firstVisibleIndex && index <= currentScrollState.focusedIndex } && it.timestamp.toLocalDate() == focusedDate && it.showDate
        }
        state.copy(
            items = items,
            addPlanItemState = addPlanItems[ADDING_PLAN_STATE_ID],
            focusedItemId = focusedDateItem?.id,
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ViewState(
            title = "",
            items = emptyList(),
            places = emptyList(),
        ),
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
        val tapped = viewState.value.items.find { it is Identifiable && it.id == itemId } ?: return
        val allowStartDateSelection =
            tapped is TripItemState.DateRangeItemState || tapped is TripItemState.InitialAddPlanItemState
        addPlanUseCase.createAddPlanItem(
            id = (tapped as? TripItemState.Replaceable)?.id,
            time = tapped.timestamp,
            dateSelectionEnabled = allowStartDateSelection,
            place = findPlaceFor(tapped),
        )
    }

    fun initialAddButtonTapped(itemId: String) {
        onAddPlanTypeSelected(AddPlanItemState.Type.entries.first())
    }

    fun emptyDateRowTapped(itemId: String) {
        val tapped = viewState.value.items.find { it is Identifiable && it.id == itemId }
        addPlanUseCase.createAddPlanItem(
            id = (tapped as Identifiable).id,
            time = tapped.timestamp,
            dateSelectionEnabled = false,
            place = findPlaceFor(tapped),
        )
    }

    fun editTapped(itemId: String) {
        if (reversibleItems.containsKey(itemId)) {
            return
        }
        val item = viewState.value.items.filterIsInstance<TripItemState.Editable>()
            .find { it.id == itemId } ?: return
        val entity = item.entity
        if (entity != null) {
            addPlanUseCase.createAddPlanItem(itemId, entity)
        } else if (item is TripItemState.PlaceItemState) {
            // A header no timed place created: editing it makes one for the place, starting there.
            val leg = trip.value?.itinerary?.legs?.firstOrNull { it.id == item.id }
            val place = leg?.place ?: return
            addPlanUseCase.createAddPlanItem(
                itemId,
                TimedPlace(
                    id = place.id,
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

    fun onAddPlanTypeSelected(type: AddPlanItemState.Type?) {
        addPlanUseCase.removeItem(ADDING_PLAN_STATE_ID)
        val currentFocusedIndex = scrollState.value.focusedIndex
        val focusedItem = if (currentFocusedIndex == -1) {
            viewState.value.items.firstOrNull()
        } else {
            viewState.value.items.getOrNull(currentFocusedIndex)
        } ?: viewState.value.items.lastOrNull()
        val timestamp = focusedItem?.timestamp ?: ZonedDateTime.now()
        if (type != null) {
            addPlanUseCase.createAddPlanItem(
                id = ADDING_PLAN_STATE_ID,
                time = timestamp,
                type = type,
                place = findPlaceForTimestamp(timestamp),
            )
        }
    }

    override fun addPlanTypeChanged(
        itemId: String,
        newType: AddPlanItemState.Type,
    ) {
        val item = addPlanUseCase.removeItem(itemId) ?: return
        if (item.type == newType) {
            return
        }
        addPlanUseCase.createAddPlanItem(
            id = itemId,
            time = item.timestamp,
            dateSelectionEnabled = item.dateSelectionEnabled,
            type = newType,
            place = findPlaceForTimestamp(item.timestamp),
        )
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
                is FlexibleDaySection -> repository.saveFlexibleSection(tripId, entity)
            }
        }
    }

    private fun TripEntity.copy(id: String = this.id) = when (this) {
        is Flight -> copy(id = id)
        is Lodging -> copy(id = id)
        is TimedPlace -> copy(id = id)
        is RestaurantReservation -> copy(id = id)
        is FlexibleDaySection -> copy(id = id)
    }

    override fun cancelEdit(itemId: String) {
        addPlanUseCase.removeItem(itemId) ?: return
    }

    override fun delete(type: AddPlanItemState.Type, itemId: String) {
        val entity = (reversibleItems[itemId] as? TripItemState.Editable)?.entity
            ?: trip.value?.flexibleSections?.firstOrNull { it.id == itemId } ?: return
        addPlanUseCase.removeItem(itemId)
        viewModelScope.launch {
            when (entity) {
                is Flight -> repository.deleteFlight(tripId, entity.id)
                is Lodging -> repository.deleteLodging(tripId, entity.id)
                is TimedPlace -> repository.deleteTimedPlace(tripId, entity.id)
                is RestaurantReservation -> repository.deleteRestaurantReservation(
                    tripId,
                    entity.id,
                )

                is FlexibleDaySection -> repository.deleteFlexibleSection(tripId, entity.id)
            }
        }
    }

    /** The place of the row's own leg; failing that, where the trip is on its day. */
    private fun findPlaceFor(item: TripItemState): Place? {
        val sectionId = (item as? TripItemState.SectionItemState)?.sectionId
        val own = trip.value?.itinerary?.legs?.firstNotNullOfOrNull { leg -> leg.place?.takeIf { it.id == sectionId } }
        return own ?: findPlaceForTimestamp(item.timestamp)
    }

    /** The place of the last leg to have started by that day. */
    private fun findPlaceForTimestamp(timestamp: ZonedDateTime): Place? {
        val legs = trip.value?.itinerary?.legs?.filter { it.place != null } ?: return null
        return (legs.lastOrNull { it.startDate <= timestamp.toLocalDate() } ?: legs.firstOrNull())?.place
    }

    fun onUpdatePreferencesTapped() {
        val destinations =
            trip.value?.getDestinations()?.map { "${it.place.name}, ${it.place.address}" }
        val dates = viewState.value.items.map { it.timestamp }
        val params = TripCreationAssistantDestination.Params(
            tripId = tripId,
            destinations = destinations ?: emptyList(),
            startDate = dates.firstOrNull()?.asISO8601String(),
            endDate = dates.lastOrNull()?.asISO8601String(),
        )
        navController.navigate(route = params)
    }

    fun onGeneratePlansTapped(itemId: String) {
        val tapped = viewState.value.items.find { it is Identifiable && it.id == itemId } ?: return
        val dates = when (tapped) {
            is TripItemState.DateRangeItemState -> tapped.getDates()
            is TripItemState.EmptyDateItemState -> listOf(tapped.timestamp)
            else -> emptyList()
        }
        viewModelScope.launch {
            val currentTrip = if (trip.value?.preferences == null) {
                val currentBackStackEntry = navController.currentBackStackEntry ?: return@launch
                onUpdatePreferencesTapped()
                val result = currentBackStackEntry.savedStateHandle.getStateFlow(
                    TripCreationAssistantDestination.RESULT_KEY_FINISHED_STATUS,
                    TripCreationAssistantDestination.FinishedStatus.NONE,
                ).filter { it != TripCreationAssistantDestination.FinishedStatus.NONE }.first()
                currentBackStackEntry.savedStateHandle.remove<TripCreationAssistantDestination.FinishedStatus>(
                    TripCreationAssistantDestination.RESULT_KEY_FINISHED_STATUS,
                )
                if (result != TripCreationAssistantDestination.FinishedStatus.COMPLETED) {
                    return@launch
                }
                trip.filter { it?.preferences != null }.first()
            } else {
                trip.value
            } ?: return@launch
            itemsGeneratingSuggestions.value = itemsGeneratingSuggestions.value + itemId
            suggestionsUseCase.getSuggestions(currentTrip, dates)
            itemsGeneratingSuggestions.value = itemsGeneratingSuggestions.value - itemId
        }
    }

    fun onSuggestedSectionDismiss(itemId: String) {
        suggestionsUseCase.dismissSuggestions(itemId)
    }

    fun onSuggestedSectionConfirmed(itemId: String) {
        val section =
            suggestionsUseCase.state.value.days.flatMap { it.sections }.find { it.id == itemId }
                ?: return
        viewModelScope.launch {
            repository.saveFlexibleSection(tripId, section)
            suggestionsUseCase.dismissSuggestions(itemId)
        }
    }

    private fun TripItemState.DateRangeItemState.getDates(): List<ZonedDateTime> {
        val itemIndex = viewState.value.items.indexOf(this)
        val startDateTime = timestamp
        val endDateTime = viewState.value.items.getOrNull(itemIndex + 1)?.timestamp ?: startDateTime

        return generateSequence(startDateTime) { it.plusDays(1) }
            .takeWhile { it.toLocalDate() < endDateTime.toLocalDate() }.toList()
    }

    /** The entity editing this row edits: that of the event it is, or the timed place that started its leg. */
    private val TripItemState.Editable.entity: TripEntity?
        get() {
            val itinerary = trip.value?.itinerary ?: return null
            return itinerary.events.filterIsInstance<ItineraryEvent.OfEntity>().firstOrNull { it.id == id }?.entity
                ?: itinerary.legs.firstNotNullOfOrNull { leg -> leg.startedBy?.takeIf { it.id == id } }
        }

    fun setScrollState(focusedIndex: Int, firstVisibleIndex: Int) {
        scrollState.value = ScrollState(focusedIndex, firstVisibleIndex)
    }

    override fun onCleared() {
        super.onCleared()
        useCaseScope.cancel()
    }

    class Factory(tripId: String) : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripViewModel(
            factoryDependencies.tripRepository,
            factoryDependencies.placeRepository,
            tripId,
            factoryDependencies.navController,
        )
    })
}
