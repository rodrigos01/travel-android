package travel.vola.android.ui.trip.viewmodel

import com.vola.android.extensions.MapFlow
import com.vola.android.extensions.MapStateFlow
import com.vola.android.extensions.get
import com.vola.android.extensions.mergeMaps
import com.vola.android.model.PlaceRepository
import com.vola.android.model.data.Flight
import com.vola.android.model.data.Lodging
import com.vola.android.model.data.Time
import com.vola.android.model.data.TripEntity
import com.vola.android.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.vola.android.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.vola.android.ui.trip.state.AddFlightItemState
import com.vola.android.ui.trip.state.AddLodgingItemState
import com.vola.android.ui.trip.state.AddPlanItemState
import com.vola.android.ui.trip.state.LodgingSearchItemState
import com.vola.android.ui.trip.state.ManualAddLodgingItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class AddPlanUseCase(
    placeRepository: PlaceRepository,
    coroutineScope: CoroutineScope,
    private val addFlightUseCase: AddFlightUseCase = AddFlightUseCase(coroutineScope = coroutineScope),
    private val addLodgingUseCase: AddLodgingUseCase = AddLodgingUseCase(
        placeRepository = placeRepository,
        coroutineScope = coroutineScope,
    ),
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase,
    LodgingSearchParamsFactory by addLodgingUseCase {

    data class StateParams(
        val dateSelectionEnabled: Boolean = true,
        val typeSelectionEnabled: Boolean = true,
        val deleteEnabled: Boolean = false,
    )

    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> {
        val items: MapFlow<String, T>

        fun addItem(id: String, time: Time, params: StateParams)
        fun addItem(id: String, entity: E, params: StateParams)

        fun removeItem(item: T)
    }

    interface EntityFactory<E : TripEntity, T : AddPlanItemState> {
        fun createEntity(item: T): E
    }

    val items: MapStateFlow<String, AddPlanItemState> = mergeMaps(
        addFlightUseCase.items,
        addLodgingUseCase.items,
    ).stateIn(coroutineScope, SharingStarted.Lazily, initialValue = emptyMap())

    fun createAddPlanItem(
        id: String?,
        time: Time,
        dateSelectionEnabled: Boolean = true,
        type: AddPlanItemState.Type = AddPlanItemState.Type.Flight,
    ) {
        type.useCase().addItem(
            id = id ?: UUID.randomUUID().toString(),
            time,
            StateParams(dateSelectionEnabled),
        )
    }

    fun createAddPlanItem(id: String, entity: TripEntity) {
        return entity.asState(id, StateParams(typeSelectionEnabled = false, deleteEnabled = true))
    }

    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) {
        val addPlanItem = items[itemId] ?: return
        if (addPlanItem.type == newType) {
            return
        }
        removeItem(addPlanItem)
        createAddPlanItem(
            itemId,
            addPlanItem.timestamp,
            addPlanItem.dateSelectionEnabled,
            newType,
        )
    }

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
        }
    }

    private val AddPlanItemState.type
        get() = when (this) {
            is AddFlightItemState -> AddPlanItemState.Type.Flight
            is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanItemState.Type.Lodging
        }

    private fun AddPlanItemState.Type.useCase(): AddItemUseCase<out TripEntity, out AddPlanItemState> =
        when (this) {
            AddPlanItemState.Type.Flight -> addFlightUseCase
            AddPlanItemState.Type.Lodging -> addLodgingUseCase
        }


    private fun TripEntity.asState(
        id: String, params: StateParams = StateParams(),
    ) {
        return when (this) {
            is Flight -> addFlightUseCase.addItem(id, this, params)
            is Lodging -> addLodgingUseCase.addItem(id, this, params)
        }
    }

    private fun AddPlanItemState.asEntity(): TripEntity {
        return when (this) {
            is AddFlightItemState -> addFlightUseCase.createEntity(this)
            is ManualAddLodgingItemState -> addLodgingUseCase.createEntity(this)
            is LodgingSearchItemState -> error("LodgingSearchItemState entity creation not implemented")
        }
    }
}
