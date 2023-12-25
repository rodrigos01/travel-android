package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Time
import java.util.UUID

class AddFlightUseCase(
    private val timeFormatter: TimeFormatter
) : AddPlanUseCase.AddItemUseCase<AddFlightUseCase.AddFlightItem> {

    private val pendingFlights: MutableMap<String, PendingFlight> = mutableMapOf()

    data class AddFlightItem(
        override val id: String,
        override val timestamp: Time,
        val departureTime: String? = null,
        val airportFromName: String? = null,
        val arrivalTime: String? = null,
        val arrivalDayOfMonth: String? = null,
        val arrivalDayOfWeek: String? = null,
        val airportToName: String? = null,
    ) : AddPlanUseCase.AddPlanItem

    data class PendingFlight(
        val departure: Time,
        val airportFrom: Airport? = null,
        val airportTo: Airport? = null,
        val arrival: Time? = null,
    ) : AddPlanUseCase.PendingData({ departure })

    override fun createItem(time: Time) = AddFlightItem(
        id = UUID.randomUUID().toString(),
        timestamp = time,
        departureTime = timeFormatter.timeString(time),
    ).also { pendingFlights[it.id] = createPendingData(it.id, time) }

    private fun createPendingData(id: String, time: Time) =
        PendingFlight(departure = time).also { pendingFlights[id] = it }

    override fun remove(item: AddFlightItem): AddPlanUseCase.PendingData? =
        pendingFlights.remove(item.id)

    fun setDepartureTime(item: AddFlightItem, hour: Int, minute: Int): AddFlightItem {
        val pending = pendingFlights[item.id] ?: return item
        val newTime = pending.departure.copy(hour = hour, minute = minute)
        pendingFlights[item.id] =
            pending.copy(departure = newTime)
        return item.copy(departureTime = timeFormatter.timeString(newTime))
    }

    fun setArrivalDay(item: AddFlightItem, day: Time): AddFlightItem {
        val pending = pendingFlights[item.id] ?: return item
        val oldTime = pending.arrival ?: pending.departure
        val newTime = oldTime.copy(
            dayOfMonth = day.dayOfMonth,
            month = day.month,
            year = day.year,
        )
        pendingFlights[item.id] =
            pending.copy(arrival = newTime)
        return item.copy(
            arrivalDayOfMonth = timeFormatter.dayOfMonthString(newTime),
            arrivalDayOfWeek = timeFormatter.dayOfWeekString(newTime),
        )
    }

    fun setArrivalTime(item: AddFlightItem, hour: Int, minute: Int): AddFlightItem {
        val pending = pendingFlights[item.id] ?: return item
        val oldTime = pending.arrival ?: pending.departure
        val newTime = oldTime.copy(hour = hour, minute = minute)
        pendingFlights[item.id] =
            pending.copy(arrival = newTime)
        return item.copy(arrivalTime = timeFormatter.timeString(newTime))
    }
}