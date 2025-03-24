package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.remove
import com.combah.travel2.extensions.set
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import kotlinx.coroutines.flow.map

class AddPlanItemStore<R : PendingData, T : AddPlanItemState> {

    data class ItemStoreData<R : PendingData>(
        val data: R, val params: AddPlanUseCase.StateParams
    )

    private val _items = MutableMapStateFlow<String, ItemStoreData<R>>()
    fun items(transform: (R, AddPlanUseCase.StateParams) -> T): MapFlow<String, T> =
        _items.map {
            it.entries.associate { (key, value) ->
                key to transform(
                    value.data,
                    value.params
                )
            }
        }

    fun addItem(data: R, stateParams: AddPlanUseCase.StateParams) =
        _items.set(
            data.id,
            ItemStoreData(data, stateParams)
        )

    fun hasItem(itemId: String): Boolean {
        return _items.value.containsKey(itemId)
    }

    fun getData(itemId: String): R? {
        return _items[itemId]?.data
    }

    fun remove(item: T) {
        _items.remove(item.id)
    }

    fun update(
        itemId: String,
        updater: (R) -> R,
    ) {
        val entry = _items[itemId] ?: error("Item with id $itemId not found in store")
        _items[itemId] = ItemStoreData(updater(entry.data), entry.params)
    }
}

