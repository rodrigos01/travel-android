package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class AddLodgingUseCase(
    private val repository: AddLodgingRepository,
    private val timeFormatter: TimeFormatter,
) :
    AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingUseCase.AddLodgingItem>,
    AddLodgingItemActionHandler {
    private val pendingLodging: MutableMap<String, PendingLodging> = mutableMapOf()

    data class AddLodgingItem(
        override val id: String,
        override val timestamp: Time,
        val name: String? = null,
        val lodgingSearchResults: List<String> = emptyList(),
        val checkInTime: String? = null,
        val minCheckOutTimeMillis: Long,
        val checkOutDayOfMonth: String,
        val checkOutDayOfWeek: String,
        val checkOutTime: String? = null,
    ) : AddPlanUseCase.AddPlanItem

    data class PendingLodging(
        val checkIn: Time,
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val checkOut: Time? = null,
        val lodgingSearchResults: List<Lodging> = emptyList(),
    ) : AddPlanUseCase.PendingData({ checkIn })

    private val _items: MutableMapStateFlow<String, AddLodgingItem> = MutableMapStateFlow()
    override val items: StateFlow<Map<String, AddLodgingItem>>
        get() = _items

    override fun createItem(time: Time) =
        AddLodgingItem(
            id = UUID.randomUUID().toString(),
            timestamp = time,
            checkInTime = timeFormatter.timeString(time),
            minCheckOutTimeMillis = time.timeInMillis,
            checkOutDayOfMonth = timeFormatter.dayOfMonthString(time),
            checkOutDayOfWeek = timeFormatter.dayOfWeekString(time),
        ).also {
            _items[it.id] = it
            pendingLodging[it.id] = createPendingData(it.id, time)
        }

    override fun setCheckInTime(itemId: String, hour: Int, minute: Int) = Unit

    override fun setCheckOutDate(itemId: String, date: Time) = Unit

    override fun setCheckoutTime(itemId: String, hour: Int, minute: Int) = Unit

    override suspend fun lodgingTextChanged(itemId: String, content: CharSequence) {
        val (item, pending) = findItem(itemId)
        val results = repository.autocomplete(content.toString())
        _items[itemId] = item.copy(
            lodgingSearchResults = results.map { it.name ?: it.address }
        )
        pendingLodging[itemId] = pending.copy(
            lodgingSearchResults = results
        )
    }

    override fun lodgingSearchResultTapped(itemId: String, index: Int) {
        val (item, pending) = findItem(itemId)
        val selected = pending.lodgingSearchResults[index]
        _items[itemId] = item.copy(
            name = selected.name ?: selected.address,
        )
        pendingLodging[itemId] = pending.copy(
            name = selected.name,
            address = selected.address,
            city = selected.city,
        )
    }

    override fun save(item: AddLodgingItem): Lodging {
        val pending = pendingLodging[item.id] ?: error("provided Id is not from this Use Case")
        pending.address ?: error("address from is not set")
        pending.city ?: error("city from is not set")
        pending.checkOut ?: error("check out from is not set")
        return Lodging(
            item.id,
            pending.address,
            pending.city,
            pending.checkIn,
            pending.checkOut,
        )
    }

    override fun remove(item: AddLodgingItem) {
        _items.remove(item.id)
        pendingLodging.remove(item.id)
    }

    private fun createPendingData(id: String, time: Time) =
        PendingLodging(checkIn = time).also { pendingLodging[id] = it }

    private fun findItem(itemId: String): Pair<AddLodgingItem, PendingLodging> {
        val item = _items[itemId] ?: error("provided Id is not from this Use Case")
        val pending = pendingLodging[itemId] ?: error("provided Id is not from this Use Case")
        return item to pending
    }
}
