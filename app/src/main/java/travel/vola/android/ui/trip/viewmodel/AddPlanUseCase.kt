package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripEntity
import travel.vola.android.ui.trip.creation.usecase.AddFlexibleSectionPersistedActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddLodgingItemState
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.AddRestaurantItemState
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.ZonedDateTime
import java.util.UUID

class AddPlanUseCase(
    placeRepository: PlaceRepository,
    private val coroutineScope: CoroutineScope,
    private val flexibleSectionUseCase: FlexibleSectionUseCase,
    private val pendingDataStore: PendingDataStore = PendingDataStore(),
    private val addFlightUseCase: AddFlightUseCase = AddFlightUseCase(pendingDataStore = pendingDataStore),
    private val addLodgingUseCase: AddLodgingUseCase = AddLodgingUseCase(
        placeRepository = placeRepository,
        pendingDataStore = pendingDataStore,
    ),
    private val addPlaceUseCase: AddPlaceUseCase = AddPlaceUseCase(pendingDataStore = pendingDataStore),
    private val addRestaurantUseCase: AddRestaurantUseCase = AddRestaurantUseCase(pendingDataStore = pendingDataStore),
) : AddPlanItemActionHandler, AddFlexibleSectionPersistedActionHandler by flexibleSectionUseCase,
        LodgingSearchParamsFactory by addLodgingUseCase {

    data class StateParams(
        val dateSelectionEnabled: Boolean = true,
        val typeSelectionEnabled: Boolean = true,
        val deleteEnabled: Boolean = false,
        val place: Place? = null,
    )

    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> {
        fun createItem(id: String, time: ZonedDateTime, params: StateParams): T
        fun createItem(id: String, entity: E, params: StateParams): T
        suspend fun onUpdated(state: T): T
    }

    interface EntityFactory<E : TripEntity, T : AddPlanItemState> {
        fun createEntity(item: T): E
    }

    private val _current = MutableStateFlow<AddPlanItemState?>(null)
    val state: StateFlow<AddPlanItemState?> = _current.asStateFlow()

    fun createAddPlanItem(
        id: String?,
        time: ZonedDateTime,
        dateSelectionEnabled: Boolean = true,
        type: AddPlanItemState.Type = AddPlanItemState.Type.Flight,
        place: Place? = null,
    ) {
        clearCurrent()
        _current.value = type.useCase().createItem(
            id ?: UUID.randomUUID().toString(),
            time,
            StateParams(dateSelectionEnabled, place = place),
        )
    }

    fun createAddPlanItem(id: String, entity: TripEntity, deleteEnabled: Boolean = true) {
        clearCurrent()
        _current.value = entity.createItem(
            id,
            StateParams(typeSelectionEnabled = false, deleteEnabled = deleteEnabled)
        )
    }

    override fun onUpdated(state: AddPlanItemState) {
        coroutineScope.launch {
            _current.value = when (state) {
                is AddFlightItemState -> addFlightUseCase.onUpdated(state)
                is AddLodgingItemState -> addLodgingUseCase.onUpdated(state)
                is AddPlaceItemState -> addPlaceUseCase.onUpdated(state)
                is AddRestaurantItemState -> addRestaurantUseCase.onUpdated(state)
                is AddFlexibleSectionItemState -> flexibleSectionUseCase.onUpdated(state)
            }
        }
    }

    override fun onSwitchToManualButtonTapped() {
        val current = _current.value as? LodgingSearchItemState ?: return
        _current.value = addLodgingUseCase.switchToManual(current)
    }

    override fun onFindLodgingButtonTapped() {
        val current = _current.value as? ManualAddLodgingItemState ?: return
        _current.value = addLodgingUseCase.switchToSearch(current)
    }

    override fun onGenerateSectionTapped() {
        val current = _current.value as? AddFlexibleSectionItemState ?: return
        clearCurrent()
        coroutineScope.launch { flexibleSectionUseCase.generateSuggestions(current.startDateTime) }
    }

    fun saveItem(): TripEntity {
        val item = _current.value ?: error("No pending item")
        val entity = item.asEntity()
        clearCurrent()
        return entity
    }

    override fun save() = Unit
    override fun cancelEdit() = clearCurrent()
    fun removeItem() = clearCurrent()
    override fun delete(type: AddPlanItemState.Type, itemId: String) = Unit

    private fun clearCurrent() {
        _current.value = null
        pendingDataStore.clear()
    }

    private fun AddPlanItemState.Type.useCase(): AddItemUseCase<out TripEntity, out AddPlanItemState> =
        when (this) {
            AddPlanItemState.Type.Flight -> addFlightUseCase
            AddPlanItemState.Type.Lodging -> addLodgingUseCase
            AddPlanItemState.Type.Place -> addPlaceUseCase
            AddPlanItemState.Type.Restaurant -> addRestaurantUseCase
            AddPlanItemState.Type.FlexibleSection -> flexibleSectionUseCase
        }

    private fun TripEntity.createItem(id: String, params: StateParams): AddPlanItemState =
        when (this) {
            is Flight -> addFlightUseCase.createItem(id, this, params)
            is Lodging -> addLodgingUseCase.createItem(id, this, params)
            is TimedPlace -> addPlaceUseCase.createItem(id, this, params)
            is RestaurantReservation -> addRestaurantUseCase.createItem(id, this, params)
            is FlexibleDaySection -> flexibleSectionUseCase.createItem(id, this, params)
        }

    private fun AddPlanItemState.asEntity(): TripEntity = when (this) {
        is AddFlightItemState -> addFlightUseCase.createEntity(this)
        is ManualAddLodgingItemState -> addLodgingUseCase.createEntity(this)
        is LodgingSearchItemState -> error("LodgingSearchItemState entity creation not implemented")
        is AddPlaceItemState -> addPlaceUseCase.createEntity(this)
        is AddRestaurantItemState -> addRestaurantUseCase.createEntity(this)
        is AddFlexibleSectionItemState -> flexibleSectionUseCase.createEntity(this)
    }
}
