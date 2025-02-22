package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.remove
import com.combah.travel2.extensions.set
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase.ItemFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddPlanItemStore : AddPlanUseCase.ItemStore {

    data class ItemStoreData(
        val state: AddPlanItemState,
        val pendingData: PendingData,
    )

    private val _items = MutableMapStateFlow<String, ItemStoreData>()
    override val items: Flow<Map<String, AddPlanItemState>> =
        _items.map { it.entries.associate { (key, value) -> key to value.state } }

    override fun addItem(data: PendingData, item: AddPlanItemState) =
        _items.set(
            item.id,
            ItemStoreData(item, data)
        )

    override fun getItem(itemId: String): AddPlanItemState? {
        return _items[itemId]?.state
    }

    override fun getData(itemId: String): PendingData? {
        return _items[itemId]?.pendingData
    }

    override fun remove(item: AddPlanItemState) {
        _items.remove(item.id)
    }

    override fun <R : PendingData, T : AddPlanItemState> update(
        itemId: String,
        itemFactory: ItemFactory<R, T>,
        updater: (R) -> R,
    ) {
        val entry = _items[itemId] ?: error("Item with id $itemId not found in store")
        val data =
            entry.pendingData as? R ?: error("Item with id $itemId is not from the expected type")
        val item = entry.state
        _items[itemId] = updater(data)
            .let {
                ItemStoreData(
                    itemFactory.createItem(
                        it,
                        item.dateSelectionEnabled,
                        item.typeSelectionEnabled,
                        item.deleteButtonEnabled,
                    ),
                    it,
                )
            }
    }
}

