package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.atTimeZone
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.creation.usecase.requireCurrent
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours

class AddFlightUseCase(
    private val pendingDataStore: PendingDataStore,
    private val repository: AddFlightRepository = AddFlightRepository(),
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightItemState>,
    AddPlanUseCase.EntityFactory<Flight, AddFlightItemState> {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        pendingDataStore.set(PendingDataStore.Entry.Flight(entityId = null, airportFrom = null, airportTo = null))
        return AddFlightItemState(
            id = id,
            timestamp = time,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = time,
                minDateTime = null,
                isTimeSet = false,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = null,
                minDateTime = time + 1.hours,
                isTimeSet = false,
                dateSelectionEnabled = true,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = false,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override fun createItem(
        id: String,
        entity: Flight,
        params: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        // Multi-segment flights collapse to their first segment - editing a connecting flight as
        // a single leg is a pre-existing limitation of this flow, not introduced here.
        val segment = entity.segments.first()
        pendingDataStore.set(
            PendingDataStore.Entry.Flight(
                entityId = entity.id,
                airportFrom = segment.airportFrom,
                airportTo = segment.airportTo,
            ),
        )
        val minArrival = segment.departure.atTimeZone(segment.airportTo.timeZone) + 1.hours
        return AddFlightItemState(
            id = id,
            timestamp = segment.departure,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = segment.departure,
                minDateTime = null,
                isTimeSet = true,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = segment.airportFrom.name,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = segment.arrival.takeIf { it >= minArrival },
                minDateTime = minArrival,
                isTimeSet = true,
                dateSelectionEnabled = true,
                locationText = segment.airportTo.name,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = true,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override suspend fun onUpdated(state: AddFlightItemState): AddFlightItemState {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Flight>()
        val fromRow = processRow(state.startState, entry.airportFrom?.iata)
        val toRow = processRow(state.endState, entry.airportTo?.iata)
        if (fromRow.resolved != null || toRow.resolved != null) {
            pendingDataStore.update { e ->
                val flight = e as PendingDataStore.Entry.Flight
                flight.copy(
                    airportFrom = fromRow.resolved ?: flight.airportFrom,
                    airportTo = toRow.resolved ?: flight.airportTo,
                )
            }
        }
        val updatedEntry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Flight>()

        val departure = updatedEntry.airportFrom?.let { fromRow.row.dateTime?.atTimeZone(it.timeZone) }
            ?: fromRow.row.dateTime
        val minArrival = (departure ?: state.timestamp) + 1.hours
        val arrival = updatedEntry.airportTo?.let { toRow.row.dateTime?.atTimeZone(it.timeZone) }
            ?: toRow.row.dateTime
        return state.copy(
            timestamp = departure ?: state.timestamp,
            startState = fromRow.row.copy(dateTime = departure),
            endState = toRow.row.copy(
                dateTime = arrival?.takeIf { it >= minArrival },
                minDateTime = minArrival,
            ),
            saveButtonEnabled = fromRow.row.isTimeSet && toRow.row.isTimeSet &&
                departure != null && arrival != null && arrival >= minArrival &&
                updatedEntry.airportFrom != null && updatedEntry.airportTo != null,
        )
    }

    private data class RowResult(val row: ManualAddPlanState, val resolved: Airport?)

    // Resolves a freshly-selected search result (only if it isn't already the resolved airport
    // in the store) or, absent a selection, fires an autocomplete search for the current text.
    private suspend fun processRow(row: ManualAddPlanState, resolvedIata: String?): RowResult {
        val selectedId = row.selectedResultId
        if (selectedId != null) {
            if (selectedId == resolvedIata) return RowResult(row, null)
            val airport = repository.details(selectedId)
            return RowResult(
                row.copy(locationText = airport.name, searchResults = emptyList()),
                airport,
            )
        }
        val text = row.locationText ?: return RowResult(row, null)
        if (text.length < 3) return RowResult(row, null)
        val results = repository.autocomplete(text).map { AutoCompleteResultState(it.iata, it.name, it.location) }
        return RowResult(row.copy(searchResults = results), null)
    }

    override fun createEntity(item: AddFlightItemState): Flight {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Flight>()
        val airportFrom = entry.airportFrom ?: error("airport from is not set")
        val airportTo = entry.airportTo ?: error("airport to is not set")
        val departure = item.startState.dateTime ?: error("departure time is not set")
        val arrival = item.endState.dateTime ?: error("arrival time is not set")
        if (!item.startState.isTimeSet) error("departure time is not set")
        if (!item.endState.isTimeSet) error("arrival time is not set")
        return Flight(
            id = entry.entityId ?: item.id,
            listOf(FlightSegment(airportFrom, departure, airportTo, arrival)),
            0.0,
        )
    }
}
