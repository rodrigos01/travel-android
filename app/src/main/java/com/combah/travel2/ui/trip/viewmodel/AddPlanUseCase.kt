package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.TripEntity
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AddPlanUseCase(
    private val addFlightUseCase: AddFlightUseCase,
    private val addLodgingUseCase: AddLodgingUseCase,
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase {

    interface ItemStore<T : AddPlanItem> {
        val items: Flow<Map<String, T>>
        fun addItem(time: Time, isFirstItem: Boolean): T
        fun remove(item: T)
    }


    interface AddItemUseCase<E : TripEntity, T : AddPlanItem> : ItemStore<T> {
        fun createAppData(item: T): E
    }

    sealed interface AddPlanItem : TripViewModel.TripItem, TripViewModel.TripItem.Identifiable,
        TripViewModel.TripItem.Timeable {
        val types: List<Type>
            get() = Type.entries

        enum class Type {
            Flight, Lodging,
        }
    }

    val items: Flow<Map<String, AddPlanItem>> = combine(
        addFlightUseCase.items, addLodgingUseCase.items
    ) { (addFlightItems, addLodgingItems) ->
        addFlightItems + addLodgingItems
    }

    fun createAddPlanItem(
        time: Time, type: AddPlanItem.Type = AddPlanItem.Type.Flight, isFirstItem: Boolean = false,
    ): AddPlanItem {
        return type.useCase.addItem(time, isFirstItem)
    }

    fun typeChanged(
        addPlanItem: AddPlanItem,
        newType: AddPlanItem.Type,
        isFirstItem: Boolean = false
    ): AddPlanItem {
        if (addPlanItem.type == newType) {
            return addPlanItem
        }
        removeItem(addPlanItem)
        return createAddPlanItem(addPlanItem.timestamp, newType, isFirstItem)
    }

    fun saveItem(addPlanItem: AddPlanItem): TripEntity = when (addPlanItem) {
        is AddFlightUseCase.AddFlightItem -> addFlightUseCase.createAppData(addPlanItem)
        is AddLodgingUseCase.AddLodgingItem -> addLodgingUseCase.createAppData(addPlanItem)
    }

    fun removeItem(addPlanItem: AddPlanItem) {
        when (addPlanItem) {
            is AddFlightUseCase.AddFlightItem -> addFlightUseCase.remove(addPlanItem)
            is AddLodgingUseCase.AddLodgingItem -> addLodgingUseCase.remove(addPlanItem)
        }
    }

    private val AddPlanItem.Type.useCase: AddItemUseCase<out TripEntity, out AddPlanItem>
        get() = when (this) {
            AddPlanItem.Type.Flight -> addFlightUseCase
            AddPlanItem.Type.Lodging -> addLodgingUseCase
        }

    private val AddPlanItem.type
        get() = when (this) {
            is AddFlightUseCase.AddFlightItem -> AddPlanItem.Type.Flight
            is AddLodgingUseCase.AddLodgingItem -> AddPlanItem.Type.Lodging
        }
}
