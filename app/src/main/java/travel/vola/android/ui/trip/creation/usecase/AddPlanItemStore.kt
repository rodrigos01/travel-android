package travel.vola.android.ui.trip.creation.usecase

import kotlinx.coroutines.flow.map
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.MutableMapStateFlow
import travel.vola.android.extensions.get
import travel.vola.android.extensions.remove
import travel.vola.android.extensions.set
import travel.vola.android.ui.trip.viewmodel.AddPlanUseCase

class AddPlanItemStore<R, T> {

    data class ItemStoreData<R>(
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

    fun addItem(key: String, data: R, stateParams: AddPlanUseCase.StateParams) =
        _items.set(
            key,
            ItemStoreData(data, stateParams)
        )

    fun getData(key: String): R? {
        return _items[key]?.data
    }

    fun remove(key: String) {
        _items.remove(key)
    }

    fun update(
        itemId: String,
        updater: (R) -> R,
    ) {
        val entry = _items[itemId] ?: error("Item with id $itemId not found in store")
        _items[itemId] = ItemStoreData(updater(entry.data), entry.params)
    }
}

