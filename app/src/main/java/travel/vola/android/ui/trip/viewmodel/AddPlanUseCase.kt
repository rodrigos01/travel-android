package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.MapStateFlow
import travel.vola.android.extensions.get
import travel.vola.android.extensions.mergeMaps
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripEntity
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.usecase.AddFlexibleSectionItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddFlightItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddLodgingItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlaceItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddRestaurantItemActionHandler
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
    tripId: String,
    tripRepository: TripRepository,
    placeRepository: PlaceRepository,
    coroutineScope: CoroutineScope,
    private val addFlightUseCase: AddFlightUseCase = AddFlightUseCase(coroutineScope = coroutineScope),
    private val addLodgingUseCase: AddLodgingUseCase = AddLodgingUseCase(
        placeRepository = placeRepository,
        coroutineScope = coroutineScope,
    ),
    private val addPlaceUseCase: AddPlaceUseCase = AddPlaceUseCase(coroutineScope = coroutineScope),
    private val addRestaurantUseCase: AddRestaurantUseCase = AddRestaurantUseCase(coroutineScope = coroutineScope),
    private val flexibleSectionUseCase: FlexibleSectionUseCase = FlexibleSectionUseCase(
        tripId,
        tripRepository,
        coroutineScope,
    ),
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase, AddPlaceItemActionHandler by addPlaceUseCase,
    AddRestaurantItemActionHandler by addRestaurantUseCase,
    LodgingSearchParamsFactory by addLodgingUseCase,
    AddFlexibleSectionItemActionHandler by flexibleSectionUseCase {

    data class StateParams(
        val dateSelectionEnabled: Boolean = true,
        val typeSelectionEnabled: Boolean = true,
        val deleteEnabled: Boolean = false,
        val place: Place? = null,
    )

    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> {
        val items: MapFlow<String, T>

        fun addItem(id: String, time: ZonedDateTime, params: StateParams)
        fun addItem(id: String, entity: E, params: StateParams)

        fun removeItem(item: T)
    }

    interface EntityFactory<E : TripEntity, T : AddPlanItemState> {
        fun createEntity(item: T): E
    }

    val items: MapStateFlow<String, AddPlanItemState> = mergeMaps(
        addFlightUseCase.items,
        addPlaceUseCase.items,
        addLodgingUseCase.items,
        addRestaurantUseCase.items,
        flexibleSectionUseCase.items,
    ).stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = emptyMap())

    fun createAddPlanItem(
        id: String?,
        time: ZonedDateTime,
        dateSelectionEnabled: Boolean = true,
        type: AddPlanItemState.Type = AddPlanItemState.Type.Flight,
        place: Place? = null,
    ) {
        type.useCase().addItem(
            id = id ?: UUID.randomUUID().toString(),
            time,
            StateParams(dateSelectionEnabled, place = place),
        )
    }

    fun createAddPlanItem(id: String, entity: TripEntity, deleteEnabled: Boolean = true) {
        return entity.asState(
            id, StateParams(typeSelectionEnabled = false, deleteEnabled = deleteEnabled)
        )
    }

    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) = Unit

    override fun save(itemId: String) = Unit

    fun saveItem(itemId: String): TripEntity {
        val addPlanItem = items[itemId] ?: error("Item with id $itemId not found in store")
        val entity = addPlanItem.asEntity()
        removeItem(addPlanItem)
        return entity
    }

    override fun cancelEdit(itemId: String) = Unit

    override fun delete(type: AddPlanItemState.Type, itemId: String) = Unit

    fun removeItem(itemId: String): AddPlanItemState? {
        val item = items[itemId] ?: return null
        removeItem(item)
        return item
    }

    private fun removeItem(item: AddPlanItemState) {
        when (item) {
            is AddFlightItemState -> addFlightUseCase.removeItem(item)
            is AddLodgingItemState -> addLodgingUseCase.removeItem(item)
            is AddPlaceItemState -> addPlaceUseCase.removeItem(item)
            is AddRestaurantItemState -> addRestaurantUseCase.removeItem(item)
            is AddFlexibleSectionItemState -> flexibleSectionUseCase.removeItem(item)
        }
    }

    private val AddPlanItemState.type
        get() = when (this) {
            is AddFlightItemState -> AddPlanItemState.Type.Flight
            is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanItemState.Type.Lodging
            is AddPlaceItemState -> AddPlanItemState.Type.Place
            is AddRestaurantItemState -> AddPlanItemState.Type.Restaurant
            is AddFlexibleSectionItemState -> AddPlanItemState.Type.FlexibleSection
        }

    private fun AddPlanItemState.Type.useCase(): AddItemUseCase<out TripEntity, out AddPlanItemState> =
        when (this) {
            AddPlanItemState.Type.Flight -> addFlightUseCase
            AddPlanItemState.Type.Lodging -> addLodgingUseCase
            AddPlanItemState.Type.Place -> addPlaceUseCase
            AddPlanItemState.Type.Restaurant -> addRestaurantUseCase
            AddPlanItemState.Type.FlexibleSection -> flexibleSectionUseCase
        }


    private fun TripEntity.asState(
        id: String, params: StateParams = StateParams(),
    ) {
        return when (this) {
            is Flight -> addFlightUseCase.addItem(id, this, params)
            is Lodging -> addLodgingUseCase.addItem(id, this, params)
            is TimedPlace -> addPlaceUseCase.addItem(id, this, params)
            is RestaurantReservation -> addRestaurantUseCase.addItem(id, this, params)
            is FlexibleDaySection -> flexibleSectionUseCase.addItem(id, this, params)
        }
    }

    private fun AddPlanItemState.asEntity(): TripEntity {
        return when (this) {
            is AddFlightItemState -> addFlightUseCase.createEntity(this)
            is ManualAddLodgingItemState -> addLodgingUseCase.createEntity(this)
            is LodgingSearchItemState -> error("LodgingSearchItemState entity creation not implemented")
            is AddPlaceItemState -> addPlaceUseCase.createEntity(this)
            is AddRestaurantItemState -> addRestaurantUseCase.createEntity(this)
            is AddFlexibleSectionItemState -> flexibleSectionUseCase.createEntity(this)
        }
    }
}
