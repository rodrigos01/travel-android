package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Time

class AddPlanUseCase(
    private val addFlightUseCase: AddFlightUseCase,
    private val addLodgingUseCase: AddLodgingUseCase,
) {

    interface AddItemUseCase {
        fun createItem(time: Time): AddPlanItem
        fun createPendingData(id: String, time: Time): PendingData

        fun removePendingData(item: AddPlanItem): PendingData?
    }

    sealed interface AddPlanItem : TripViewModel.TripItem,
        TripViewModel.TripItem.Identifiable {
        val types: List<Type>
            get() = Type.entries

        enum class Type {
            Flight,
            Lodging,
        }
    }

    sealed class PendingData(private val getTimestamp: () -> Time) {
        val timestamp
            get() = getTimestamp()

    }

    fun createAddPlanItem(time: Time): AddPlanItem {
        return createAddPlanItem(AddPlanItem.Type.Flight, time).also {
            it.type.useCase.createPendingData(it.id, time)
        }
    }

    fun typeChanged(addPlanItem: AddPlanItem, newType: AddPlanItem.Type): AddPlanItem {
        if (addPlanItem.type == newType) {
            return addPlanItem
        }
        val originalTime =
            addPlanItem.type.useCase.removePendingData(addPlanItem)?.timestamp ?: return addPlanItem
        return createAddPlanItem(newType, originalTime).also {
            it.type.useCase.createPendingData(it.id, originalTime)
        }
    }

    private fun createAddPlanItem(type: AddPlanItem.Type, time: Time): AddPlanItem {
        return type.useCase.createItem(time)
    }

    private val AddPlanItem.Type.useCase: AddItemUseCase
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