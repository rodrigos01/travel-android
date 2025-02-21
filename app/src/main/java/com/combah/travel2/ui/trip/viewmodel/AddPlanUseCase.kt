package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.combineWithoutWaiting
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
import kotlinx.coroutines.flow.Flow

class AddPlanUseCase(
    private val addFlightUseCase: AddFlightUseCase,
    private val addLodgingUseCase: AddLodgingUseCase,
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase {

    interface ItemStore<E : TripEntity, T : AddPlanItemState> {
        val items: Flow<Map<String, T>>
        fun addItem(time: Time, dateSelectionEnabled: Boolean = false): T
        fun addItem(entity: E): T
        fun remove(item: T)
    }


    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> : ItemStore<E, T> {
        fun createAppData(item: T): E
    }

    val items = combineWithoutWaiting(
        addFlightUseCase.items, emptyMap(), addLodgingUseCase.items, emptyMap(),
    ) { addFlightItems, addLodgingItems ->
        addFlightItems + addLodgingItems
    }

    fun createAddPlanItem(
        time: Time,
        dateSelectionEnabled: Boolean = false,
        type: AddPlanItemState.Type = AddPlanItemState.Type.Flight
    ): AddPlanItemState {
        return type.useCase.addItem(time, dateSelectionEnabled)
    }

    fun createAddPlanItem(entity: TripEntity): AddPlanItemState {
        return when (entity) {
            is Flight -> addFlightUseCase.addItem(entity)
            is Lodging -> addLodgingUseCase.addItem(entity)
        }
    }

    fun typeChanged(
        addPlanItem: AddPlanItemState,
        newType: AddPlanItemState.Type
    ): AddPlanItemState {
        if (addPlanItem.type == newType) {
            return addPlanItem
        }
        removeItem(addPlanItem)
        return createAddPlanItem(
            addPlanItem.timestamp,
            false,
            newType,
        )
    }

    fun saveItem(addPlanItem: AddPlanItemState): TripEntity = when (addPlanItem) {
        is AddFlightItemState -> addFlightUseCase.createAppData(addPlanItem)
        is AddLodgingItemState -> addLodgingUseCase.createAppData(addPlanItem)
    }

    fun removeItem(addPlanItem: AddPlanItemState) {
        when (addPlanItem) {
            is AddFlightItemState -> addFlightUseCase.remove(addPlanItem)
            is AddLodgingItemState -> addLodgingUseCase.remove(addPlanItem)
        }
    }

    private val AddPlanItemState.Type.useCase: AddItemUseCase<out TripEntity, out AddPlanItemState>
        get() = when (this) {
            AddPlanItemState.Type.Flight -> addFlightUseCase
            AddPlanItemState.Type.Lodging -> addLodgingUseCase
        }

    private val AddPlanItemState.type
        get() = when (this) {
            is AddFlightItemState -> AddPlanItemState.Type.Flight
            is AddLodgingItemState -> AddPlanItemState.Type.Lodging
        }
}
