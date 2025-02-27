package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.TripEntity
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.AddPlanItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

class AddPlanUseCase private constructor(
    private val addFlightUseCase: AddFlightUseCase,
    private val addLodgingUseCase: AddLodgingUseCase,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase {

    constructor() : this(AddFlightUseCase(), AddLodgingUseCase())

    data class StateParams(
        val dateSelectionEnabled: Boolean = true,
        val typeSelectionEnabled: Boolean = true,
        val deleteEnabled: Boolean = false,
    )

    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> {
        val items: MapFlow<String, T>

        fun addItem(time: Time, params: StateParams): AddPlanItemState
        fun addItem(entity: E, params: StateParams): T

        fun removeItem(item: T)
        fun createEntity(item: T): E
    }

    val items = merge(
        addFlightUseCase.items,
        addLodgingUseCase.items,
    ).scan(emptyMap<String, AddPlanItemState>()) { items, newValue ->
        items + newValue
    }.stateIn(coroutineScope, SharingStarted.Lazily, initialValue = emptyMap())

    fun createAddPlanItem(
        time: Time,
        dateSelectionEnabled: Boolean = true,
        type: AddPlanItemState.Type = AddPlanItemState.Type.Flight
    ): AddPlanItemState {
        return type.useCase().addItem(time, StateParams(dateSelectionEnabled))
    }

    fun createAddPlanItem(entity: TripEntity): AddPlanItemState {
        val item = entity.asState(StateParams(typeSelectionEnabled = false, deleteEnabled = true))
        return item
    }

    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) {
        val addPlanItem = items[itemId] ?: return
        if (addPlanItem.type == newType) {
            return
        }
        removeItem(addPlanItem)
        createAddPlanItem(
            addPlanItem.timestamp,
            dateSelectionEnabled = false,
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
            is AddLodgingItemState -> AddPlanItemState.Type.Lodging
        }

    private fun AddPlanItemState.Type.useCase() = when (this) {
        AddPlanItemState.Type.Flight -> addFlightUseCase
        AddPlanItemState.Type.Lodging -> addLodgingUseCase
    }


    private fun TripEntity.asState(
        params: StateParams = StateParams(),
    ): AddPlanItemState {
        return when (this) {
            is Flight -> addFlightUseCase.addItem(this, params)

            is Lodging -> addLodgingUseCase.addItem(this, params)
        }
    }

    private fun AddPlanItemState.asEntity(): TripEntity {
        return when (this) {
            is AddFlightItemState -> addFlightUseCase.createEntity(this)
            is AddLodgingItemState -> addLodgingUseCase.createEntity(this)
        }
    }
}
