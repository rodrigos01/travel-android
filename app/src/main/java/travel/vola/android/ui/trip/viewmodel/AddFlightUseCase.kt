package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.atTimeZone
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.ui.trip.creation.usecase.AddFlightItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingFlight
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours

class AddFlightUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingFlight, AddFlightItemState> = AddPlanItemStore(),
    private val repository: AddFlightRepository = AddFlightRepository(),
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightItemState>,
    AddPlanUseCase.EntityFactory<Flight, AddFlightItemState>, AddFlightItemActionHandler {

    override val items: MapFlow<String, AddFlightItemState> = itemStore.items(::createItem)

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingFlight(
            id = id,
            departure = time,
        )
        itemStore.addItem(data, params)
    }

    override fun addItem(id: String, entity: Flight, params: AddPlanUseCase.StateParams) {
        entity.segments.forEach { segment ->
            val data = PendingFlight(
                id = id,
                entityId = entity.id,
                departure = segment.departure,
                departureTimeSet = true,
                airportFrom = segment.airportFrom,
                airportTo = segment.airportTo,
                arrival = segment.arrival,
                arrivalTimeSet = true,
            )
            itemStore.addItem(data, params)
        }
    }

    private fun createItem(
        data: PendingFlight,
        stateParams: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        val minArrival =
            (data.airportTo?.let { data.departure.atTimeZone(it.timeZone) }
                ?: data.departure) + 1.hours
        return AddFlightItemState(
            id = data.id,
            timestamp = data.departure,
            startState = ManualAddPlanState(
                dateTime = data.departure,
                minDateTime = null,
                isTimeSet = data.departureTimeSet,
                dateSelectionEnabled = stateParams.dateSelectionEnabled,
                locationText = data.airportFrom?.name,
                searchResults = data.airportFromSearchResults.map {
                    AutoCompleteResultState(it.name, it.location)
                },
            ),
            endState = ManualAddPlanState(
                dateTime = data.arrival?.takeIf { it >= minArrival },
                minDateTime = minArrival,
                isTimeSet = data.arrivalTimeSet,
                dateSelectionEnabled = true,
                locationText = data.airportTo?.name,
                searchResults = data.airportToSearchResults.map {
                    AutoCompleteResultState(
                        it.name, it.location
                    )
                },
            ),
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            deleteButtonEnabled = stateParams.deleteEnabled,
            saveButtonEnabled = data.departureTimeSet && data.arrivalTimeSet && data.arrival?.let { it >= minArrival } ?: false && data.airportFrom != null && data.airportTo != null,
        )
    }

    override fun removeItem(item: AddFlightItemState) {
        itemStore.remove(item)
    }

    override fun createEntity(item: AddFlightItemState): Flight {
        val data = itemStore.getData(item.id) ?: error("Item not found in store")
        data.airportFrom ?: error("airport from is not set")
        data.airportTo ?: error("airport to is not set")
        data.arrival ?: error("arival time is not set")
        if (!data.arrivalTimeSet) {
            error("arival time is not set")
        }
        if (!data.departureTimeSet) {
            error("departure time is not set")
        }
        return Flight(
            id = data.entityId ?: data.id,
            listOf(
                FlightSegment(
                    data.airportFrom,
                    data.departure,
                    data.airportTo,
                    data.arrival,
                )
            ),
            0.0,
        )
    }

    private val autoCompleteScope = MutexScope(coroutineScope.coroutineContext)
    override fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence,
    ) {
        if (content.length < 3) {
            return
        }
        autoCompleteScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) {
                it.copy(airportFromSearchResults = results)
            }
        }
    }

    override fun airportToSearchTextChanged(
        itemId: String, content: CharSequence,
    ) {
        if (content.length < 3) {
            return
        }
        autoCompleteScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) {
                it.copy(airportToSearchResults = results)
            }
        }
    }

    override fun onUpdated(
        itemId: String,
        departureTime: ZonedDateTime,
        departureTimeSelected: Boolean,
        selectedDepartureSearchResultIndex: Int,
        arrivalTime: ZonedDateTime?,
        arrivalTimeSelected: Boolean,
        selectedArrivalSearchResultIndex: Int,
    ) {
        val current = itemStore.getData(itemId)
        coroutineScope.launch {
            val (selectedDepartureAirport, selectedArrivalAirport) = awaitAll(
                async {
                    current?.airportFromSearchResults?.getOrNull(selectedDepartureSearchResultIndex)?.iata?.let { selectedId ->
                        repository.details(selectedId)
                    }
                },
                async {
                    current?.airportToSearchResults?.getOrNull(selectedArrivalSearchResultIndex)?.iata?.let { selectedId ->
                        repository.details(selectedId)
                    }
                },
            )
            itemStore.update(itemId) { data ->
                val airportFrom = selectedDepartureAirport ?: data.airportFrom
                val airportTo = selectedArrivalAirport ?: data.airportTo
                PendingFlight(
                    id = data.id,
                    entityId = data.entityId,
                    departure = departureTime.update(
                        timeZone = airportFrom?.timeZone?.toZoneId() ?: data.departure.zone
                    ),
                    departureTimeSet = departureTimeSelected,
                    airportFrom = airportFrom,
                    arrival = arrivalTime?.update(
                        timeZone = airportTo?.timeZone?.toZoneId() ?: data.arrival?.zone
                        ?: data.departure.zone
                    ),
                    arrivalTimeSet = arrivalTimeSelected,
                    airportTo = airportTo,
                )
            }
        }
    }
}
