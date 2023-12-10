package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import java.util.UUID

class AddLodgingUseCase(private val timeFormatter: TimeFormatter) : AddPlanUseCase.AddItemUseCase {
    private val pendingLodging: MutableMap<String, PendingLodging> = mutableMapOf()

    data class AddLodgingItem(
        override val id: String,
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
            checkInTime = timeFormatter.timeString(time),
        )

    override fun createPendingData(id: String, time: Time) =
        PendingLodging(checkIn = time).also { pendingLodging[id] = it }

    override fun removePendingData(item: AddPlanUseCase.AddPlanItem): AddPlanUseCase.PendingData? =
        pendingLodging.remove(item.id)
}