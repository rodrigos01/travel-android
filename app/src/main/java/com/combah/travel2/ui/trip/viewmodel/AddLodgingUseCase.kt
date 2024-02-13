package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AddLodgingUseCase(private val timeFormatter: TimeFormatter) :
    AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingUseCase.AddLodgingItem>,
    AddLodgingItemActionHandler {
    private val pendingLodging: MutableMap<String, PendingLodging> = mutableMapOf()

    data class AddLodgingItem(
        override val id: String,
        override val timestamp: Time,
        val name: String? = null,
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
    ) : AddPlanUseCase.PendingData({ checkIn })

    private val _items: MutableMapStateFlow<String, AddLodgingItem> = MutableMapStateFlow()
    override val items: Flow<Map<String, AddPlanUseCase.AddPlanItem>>
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

    override fun setCheckInTime(itemId: String, hour: Int, minute: Int) = Unit

    override fun setCheckOutDate(itemId: String, date: Time) = Unit

    override fun setCheckoutTime(itemId: String, hour: Int, minute: Int) = Unit

    override fun lodgingTextChanged(itemId: String, content: CharSequence) = Unit

    override fun lodgingSearchResultTapped(itemId: String, index: Int) = Unit

    private fun createPendingData(id: String, time: Time) =
        PendingLodging(checkIn = time).also { pendingLodging[id] = it }
}
