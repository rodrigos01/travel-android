package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import java.util.UUID

class AddPlanUseCase(private val timeFormatter: TimeFormatter) {

    private val pendingData: MutableMap<String, PendingData> = mutableMapOf()

    sealed class AddPlanItem(val type: Type) : TripViewModel.TripItem,
        TripViewModel.TripItem.Identifiable {
        val types: List<Type>
            get() = Type.entries

        data class Flight(
            override val id: String,
            val departureTime: String? = null,
            val airportFromName: String? = null,
            val arrivalTime: String? = null,
            val arrivalDayOfMonth: String? = null,
            val arrivalDayOfWeek: String? = null,
            val airportToName: String? = null,
        ) : AddPlanItem(Type.Flight)

        data class Lodging(
            override val id: String,
            val checkInTime: String? = null,
            val checkOutDayOfMonth: String? = null,
            val checkOutDayOfWeek: String? = null,
            val checkOutTime: String? = null,
        ) : AddPlanItem(Type.Lodging)

        enum class Type {
            Flight,
            Lodging,
        }
    }

    sealed class PendingData(private val getTimestamp: () -> Time) {
        val timestamp
            get() = getTimestamp()

        data class Flight(
            val departure: Time,
            val airportFrom: Airport? = null,
            val airportTo: Airport? = null,
            val arrival: Time? = null,
        ) : PendingData({ departure })

        data class Lodging(
            val checkIn: Time,
            val name: String? = null,
            val address: String? = null,
            val city: Place? = null,
            val checkOut: Time? = null,
        ) : PendingData({ checkIn })
    }

    fun createAddPlanItem(time: Time): AddPlanItem {
        return createAddPlanItem(AddPlanItem.Type.Flight, time).also {
            addToPendingData(it.id, it.type, time)
        }
    }

    fun typeChanged(addPlanItem: AddPlanItem, newType: AddPlanItem.Type): AddPlanItem {
        if (addPlanItem.type == newType) {
            return addPlanItem
        }
        val originalTime = pendingData.remove(addPlanItem.id)?.timestamp ?: return addPlanItem
        return createAddPlanItem(newType, originalTime).also {
            addToPendingData(it.id, it.type, originalTime)
        }
    }

    private fun createAddPlanItem(type: AddPlanItem.Type, time: Time): AddPlanItem {
        val id = UUID.randomUUID().toString()
        return when (type) {
            AddPlanItem.Type.Flight -> AddPlanItem.Flight(
                id,
                departureTime = timeFormatter.timeString(time),
            )

            AddPlanItem.Type.Lodging -> AddPlanItem.Lodging(
                id,
                checkInTime = timeFormatter.timeString(time),
            )
        }
    }

    private fun addToPendingData(id: String, type: AddPlanItem.Type, timestamp: Time) {
        pendingData[id] = when (type) {
            AddPlanItem.Type.Flight -> PendingData.Flight(departure = timestamp)
            AddPlanItem.Type.Lodging -> PendingData.Lodging(checkIn = timestamp)
        }
    }
}