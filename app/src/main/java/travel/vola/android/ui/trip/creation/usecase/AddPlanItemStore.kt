package travel.vola.android.ui.trip.creation.usecase

import com.vola.android.extensions.MapFlow
import com.vola.android.extensions.MutableMapStateFlow
import com.vola.android.extensions.get
import com.vola.android.extensions.remove
import com.vola.android.extensions.set
import com.vola.android.ui.trip.state.AddPlanItemState
import com.vola.android.ui.trip.viewmodel.AddPlanUseCase
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

