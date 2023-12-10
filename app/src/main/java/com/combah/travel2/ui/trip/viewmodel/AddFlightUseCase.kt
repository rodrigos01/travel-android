package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Time
import java.util.UUID

class AddFlightUseCase(private val timeFormatter: TimeFormatter) : AddPlanUseCase.AddItemUseCase {

    private val pendingFlights: MutableMap<String, PendingFlight> = mutableMapOf()

    data class AddFlightItem(
        override val id: String,
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
        departureTime = timeFormatter.timeString(time),
    )

    override fun createPendingData(id: String, time: Time): AddPlanUseCase.PendingData =
        PendingFlight(departure = time).also { pendingFlights[id] = it }

    override fun removePendingData(item: AddPlanUseCase.AddPlanItem): AddPlanUseCase.PendingData? =
        pendingFlights.remove(item.id)
}