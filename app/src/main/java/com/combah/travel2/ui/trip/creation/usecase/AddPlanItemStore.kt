package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.remove
import com.combah.travel2.extensions.set
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.TripEntity
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AddPlanItemStore<Entity : TripEntity, Data : AddPlanItemStore.AddPlanData, Item : AddPlanUseCase.AddPlanItem>(
    private val dataFactory: DataFactory<Entity, Data>,
    private val itemFactory: ItemFactory<Item, Data>,
) : AddPlanUseCase.ItemStore<Entity, Item> {

    interface AddPlanData {
        val id: String
    }

    fun interface ItemFactory<Item, Data : AddPlanData> {
        fun createItem(data: Data, startDateSelectionEnabled: Boolean): Item
    }

    interface DataFactory<Entity : TripEntity, Data : AddPlanData> {
        fun createData(time: Time): Data
        fun createData(entity: Entity): Data
    }

    data class ItemStoreData<Item, Data>(
        val item: Item,
        val data: Data,
    )

    private val _items: MutableStateFlow<Map<String, ItemStoreData<Item, Data>>> =
        MutableMapStateFlow()

    override val items: Flow<Map<String, Item>>
        get() = _items.map { it.entries.associate { (key, value) -> key to value.item } }

    override fun addItem(time: Time, startDateSelectionEnabled: Boolean): Item {
        return addItem(dataFactory.createData(time), startDateSelectionEnabled)
    }

    override fun addItem(entity: Entity): Item {
        return addItem(dataFactory.createData(entity))
    }

    private fun addItem(data: Data, startDateSelectionEnabled: Boolean = true): Item {
        val item = itemFactory.createItem(data, startDateSelectionEnabled)
        _items[data.id] = ItemStoreData(item, data)
        return item
    }

    fun get(itemId: String) = _items[itemId]?.data

    fun update(
        itemId: String,
        updater: (Data) -> Data,
    ) {
        val data = findItem(itemId) ?: error("Item with id $itemId not found in store")
        _items[itemId] = updater(data.data)
            .let {
                ItemStoreData(
                    itemFactory.createItem(it, data.item.startDateSelectionEnabled),
                    it,
                )
            }
    }

    override fun remove(item: Item) {
        _items.remove(item.id)
    }

    private fun findItem(itemId: String): ItemStoreData<Item, Data>? {
        return _items[itemId]
    }

    class Factory<Entity : TripEntity, Data : AddPlanData, Item : AddPlanUseCase.AddPlanItem> {
        fun create(
            dataFactory: DataFactory<Entity, Data>,
            itemFactory: ItemFactory<Item, Data>
        ) = AddPlanItemStore(dataFactory, itemFactory)
    }
}

