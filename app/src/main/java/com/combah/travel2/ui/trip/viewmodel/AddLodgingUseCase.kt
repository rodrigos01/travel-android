package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
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
        val checkOutDayOfMonth: String? = null,
        val checkOutDayOfWeek: String? = null,
        val checkOutTime: String? = null,
    ) : AddPlanUseCase.AddPlanItem

    data class PendingLodging(
        val checkIn: Time,
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val checkOut: Time? = null,
    ) : AddPlanUseCase.PendingData({ checkIn })

    override fun createItem(time: Time) =
        AddLodgingItem(
            id = UUID.randomUUID().toString(),
            timestamp = time,
            checkInTime = timeFormatter.timeString(time),
        ).also { pendingLodging[it.id] = createPendingData(it.id, time) }

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

    private fun createPendingData(id: String, time: Time) =
        PendingLodging(checkIn = time).also { pendingLodging[id] = it }

    override fun remove(item: AddLodgingItem) {
        pendingLodging.remove(item.id)
    }
}