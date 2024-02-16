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

    interface AddItemUseCase<E : TripEntity, T : AddPlanItem> {

        val items: Flow<Map<String, AddPlanItem>>
        fun createItem(time: Time): AddPlanItem
        fun remove(item: T)

        fun save(item: T): E
    }

    sealed interface AddPlanItem : TripViewModel.TripItem, TripViewModel.TripItem.Identifiable,
        TripViewModel.TripItem.Timeable {
        val types: List<Type>
            get() = Type.entries

        enum class Type {
            Flight, Lodging,
        }
    }

    sealed class PendingData(private val getTimestamp: () -> Time) {
        val timestamp
            get() = getTimestamp()

    }

    val items: Flow<Map<String, AddPlanItem>> = combine(
        addFlightUseCase.items, addLodgingUseCase.items
    ) { (addFlightItems, addLodgingItems) ->
        addFlightItems + addLodgingItems
    }

    fun createAddPlanItem(
        time: Time, type: AddPlanItem.Type = AddPlanItem.Type.Flight
    ): AddPlanItem {
        return type.useCase.createItem(time)
    }

    fun typeChanged(addPlanItem: AddPlanItem, newType: AddPlanItem.Type): AddPlanItem {
        if (addPlanItem.type == newType) {
            return addPlanItem
        }
        removeItem(addPlanItem)
        return createAddPlanItem(addPlanItem.timestamp, newType)
    }

    fun saveItem(addPlanItem: AddPlanItem): TripEntity = when (addPlanItem) {
        is AddFlightUseCase.AddFlightItem -> addFlightUseCase.save(addPlanItem)
        is AddLodgingUseCase.AddLodgingItem -> addLodgingUseCase.save(addPlanItem)
    }

    private fun removeItem(addPlanItem: AddPlanItem) {
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
