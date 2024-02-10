package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AddFlightUseCase(
    private val addFlightRepository: AddFlightRepository, private val timeFormatter: TimeFormatter
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightUseCase.AddFlightItem>,
    AddFlightItemActionHandler {

    data class AddFlightItem(
        override val id: String,
        override val timestamp: Time,
        val departureTime: String? = null,
        val airportFromName: String? = null,
        val airportFromSearchResults: List<String> = emptyList(),
        val arrivalTime: String? = null,
        val arrivalDayOfMonth: String,
        val arrivalDayOfWeek: String,
        val airportToName: String? = null,
        val airportToSearchResults: List<String> = emptyList(),
    ) : AddPlanUseCase.AddPlanItem

    data class PendingFlight(
        val departure: Time,
        val airportFromSearchResults: List<Airport> = emptyList(),
        val airportFrom: Airport? = null,
        val airportToSearchResults: List<Airport> = emptyList(),
        val airportTo: Airport? = null,
        val arrival: Time? = null,
    ) : AddPlanUseCase.PendingData({ departure })

    private val _items: MutableMapStateFlow<String, AddFlightItem> = MutableMapStateFlow()
    override val items: Flow<Map<String, AddPlanUseCase.AddPlanItem>>
        get() = _items

    private val pendingFlights: MutableMap<String, PendingFlight> = mutableMapOf()

    override fun createItem(time: Time) = AddFlightItem(
        id = UUID.randomUUID().toString(),
        timestamp = time,
        arrivalDayOfWeek = timeFormatter.dayOfWeekString(time),
        arrivalDayOfMonth = timeFormatter.dayOfMonthString(time),
    ).also {
        _items[it.id] = it
        pendingFlights[it.id] = createPendingData(it.id, time)
    }

    private fun createPendingData(id: String, time: Time) =
        PendingFlight(departure = time).also { pendingFlights[id] = it }

    override fun remove(item: AddFlightItem) {
        _items.remove(item.id)
        pendingFlights.remove(item.id)
    }

    override fun setDepartureTime(itemId: String, hour: Int, minute: Int): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val newTime = pending.departure.copy(hour = hour, minute = minute)
        pendingFlights[itemId] = pending.copy(departure = newTime)
        return item.copy(departureTime = timeFormatter.timeString(newTime))
            .also { _items[itemId] = it }
    }

    override fun setArrivalDate(itemId: String, date: Time): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val oldTime = pending.arrival ?: pending.departure
        val newTime = oldTime.copy(
            dayOfMonth = date.dayOfMonth,
            month = date.month,
            year = date.year,
        )
        pendingFlights[itemId] = pending.copy(arrival = newTime)
        return item.copy(
            arrivalDayOfMonth = timeFormatter.dayOfMonthString(newTime),
            arrivalDayOfWeek = timeFormatter.dayOfWeekString(newTime),
        ).also { _items[itemId] = it }
    }

    override fun setArrivalTime(itemId: String, hour: Int, minute: Int): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val oldTime = pending.arrival ?: pending.departure
        val newTime = oldTime.copy(hour = hour, minute = minute)
        pendingFlights[itemId] = pending.copy(arrival = newTime)
        return item.copy(arrivalTime = timeFormatter.timeString(newTime))
            .also { _items[itemId] = it }
    }

    override suspend fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    ): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val results = addFlightRepository.autocomplete(content.toString())
        pendingFlights[itemId] = pending.copy(
            airportFromSearchResults = results,
        )
        return item.copy(airportFromSearchResults = results.map { it.name })
            .also { _items[itemId] = it }
    }

    override fun airportFromSearchResultTapped(itemId: String, index: Int): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val selectedAirport = pending.airportFromSearchResults[index]
        pendingFlights[itemId] = pending.copy(
            airportFromSearchResults = emptyList(),
            airportFrom = selectedAirport,
        )
        return item.copy(
            airportFromName = selectedAirport.name
        ).also { _items[itemId] = it }
    }

    override suspend fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    ): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val results = addFlightRepository.autocomplete(content.toString())
        pendingFlights[itemId] = pending.copy(
            airportToSearchResults = results,
        )
        return item.copy(airportToSearchResults = results.map { it.name })
            .also { _items[itemId] = it }
    }

    override fun airportToSearchResultTapped(itemId: String, index: Int): AddFlightItem {
        val (item, pending) = findItem(itemId)
        val selectedAirport = pending.airportToSearchResults[index]
        pendingFlights[itemId] = pending.copy(
            airportToSearchResults = emptyList(),
            airportTo = selectedAirport,
        )
        return item.copy(
            airportToName = selectedAirport.name
        ).also { _items[itemId] = it }
    }

    override fun save(item: AddFlightItem): Flight {
        val pending = pendingFlights[item.id] ?: error("provided Id is not from this Use Case")
        pending.airportFrom ?: error("airport from is not set")
        pending.airportTo ?: error("airport to is not set")
        pending.arrival ?: error("arival time is not set")
        remove(item)
        return Flight(
            item.id,
            listOf(
                FlightSegment(
                    pending.airportFrom,
                    pending.departure,
                    pending.airportTo,
                    pending.arrival,
                )
            ),
            0.0,
        )
    }

    private fun findItem(itemId: String): Pair<AddFlightItem, PendingFlight> {
        val item = _items[itemId] ?: error("provided Id is not from this Use Case")
        val pending = pendingFlights[itemId] ?: error("provided Id is not from this Use Case")
        return item to pending
    }
}