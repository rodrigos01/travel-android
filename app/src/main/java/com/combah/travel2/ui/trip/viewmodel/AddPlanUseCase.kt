package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.combineWithoutWaiting
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.TripEntity
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.PendingData
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.AddPlanItemState
import kotlinx.coroutines.flow.Flow

class AddPlanUseCase(
    private val itemStore: ItemStore,
    private val addFlightUseCase: AddFlightUseCase,
    private val addLodgingUseCase: AddLodgingUseCase,
) : AddPlanItemActionHandler, AddFlightItemActionHandler by addFlightUseCase,
    AddLodgingItemActionHandler by addLodgingUseCase {

    interface ItemStore {
        val items: Flow<Map<String, AddPlanItemState>>
        fun addItem(data: PendingData, item: AddPlanItemState)
        fun <R : PendingData, T : AddPlanItemState> update(
            itemId: String,
            itemFactory: ItemFactory<R, T>,
            updater: (R) -> R,
        )

        fun getItem(itemId: String): AddPlanItemState?
        fun getData(itemId: String): PendingData?

        fun remove(item: AddPlanItemState)
    }

    interface ItemFactory<R : PendingData, T : AddPlanItemState> {
        fun createItem(
            data: R,
            dateSelectionEnabled: Boolean = true,
            typeSelectionEnabled: Boolean = true,
            deleteEnabled: Boolean = false,
        ): T
    }

    interface AddItemUseCase<E : TripEntity, T : AddPlanItemState, R : PendingData> :
        ItemFactory<R, T> {
        fun createData(time: Time): R
        fun createData(entity: E): R
        fun createAppData(data: R): E
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
        val data =
            type.useCase()
                .createData(time)
        val item = type.useCase().createItem(data, dateSelectionEnabled)
        itemStore.addItem(data, item)
        return item
    }

    fun createAddPlanItem(entity: TripEntity): AddPlanItemState {
        val data =
            entity.type.useCase()
                .createData(entity)
        val item = entity.type.useCase().createItem(
            data,
            dateSelectionEnabled = false,
            typeSelectionEnabled = false,
            deleteEnabled = true,
        )
        itemStore.addItem(data, item)
        return item
    }

    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) {
        val addPlanItem = itemStore.getItem(itemId) ?: return
        if (addPlanItem.type == newType) {
            return
        }
        itemStore.remove(addPlanItem)
        createAddPlanItem(
            addPlanItem.timestamp,
            false,
            newType,
        )
    }

    override fun save(itemId: String) = Unit

    fun saveItem(itemId: String): TripEntity {
        val addPlanItem =
            itemStore.getItem(itemId) ?: error("Item with id $itemId not found in store")
        val data =
            itemStore.getData(addPlanItem.id) ?: error("Item with id $itemId not found in store")
        return data.type.useCase().createAppData(data).also {
            itemStore.remove(addPlanItem)
        }
    }

    override fun cancelEdit(itemId: String) = Unit

    override fun delete(type: AddPlanItemState.Type, itemId: String) = Unit

    fun removeItem(itemId: String): AddPlanItemState? {
        val item = itemStore.getItem(itemId) ?: return null
        itemStore.remove(item)
        return item
    }

    private val PendingData.type
        get() = when (this) {
            is PendingData.PendingFlight -> AddPlanItemState.Type.Flight
            is PendingData.PendingLodging -> AddPlanItemState.Type.Lodging
        }

    private val AddPlanItemState.type
        get() = when (this) {
            is AddFlightItemState -> AddPlanItemState.Type.Flight
            is AddLodgingItemState -> AddPlanItemState.Type.Lodging
        }

    private val TripEntity.type
        get() = when (this) {
            is Flight -> AddPlanItemState.Type.Flight
            is Lodging -> AddPlanItemState.Type.Lodging
        }

    private fun AddPlanItemState.Type.useCase() =
        when (this) {
            AddPlanItemState.Type.Flight -> addFlightUseCase
            AddPlanItemState.Type.Lodging -> addLodgingUseCase
        } as AddItemUseCase<TripEntity, AddPlanItemState, PendingData>
}
