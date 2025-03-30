package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.atTimeZone
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.ui.trip.creation.usecase.AddFlightItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingFlight
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import kotlin.time.Duration.Companion.hours

class AddFlightUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingFlight, AddFlightItemState> = AddPlanItemStore(),
    private val repository: AddFlightRepository = AddFlightRepository(),
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightItemState>,
    AddPlanUseCase.EntityFactory<Flight, AddFlightItemState>,
    AddFlightItemActionHandler {

    override val items: MapFlow<String, AddFlightItemState> = itemStore.items(::createItem)

    override fun setDepartureTime(itemId: String, time: Time) {
        itemStore.update(itemId) {
            it.copy(
                departure = it.departure.update(
                    dayOfMonth = time.dayOfMonth,
                    month = time.month,
                    year = time.year,
                    hour = time.hour,
                    minute = time.minute,
                ),
            )
        }
    }

    override fun setArrivalTime(itemId: String, time: Time) {
        itemStore.update(itemId) {
            it.copy(
                arrival = (it.arrival ?: it.departure).update(
                    dayOfMonth = time.dayOfMonth,
                    month = time.month,
                    year = time.year,
                    hour = time.hour,
                    minute = time.minute,
                )
            )
        }
    }

    override fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    ) {
        coroutineScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) {
                it.copy(airportFromSearchResults = results)
            }
        }
    }

    override fun airportFromSearchResultTapped(itemId: String, index: Int) {
        itemStore.update(itemId) {
            val selected = it.airportFromSearchResults.getOrNull(index)
            it.copy(
                airportFrom = selected,
                airportFromSearchResults = emptyList()
            )
        }
    }

    override fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    ) {
        coroutineScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) {
                it.copy(airportToSearchResults = results)
            }
        }
    }

    override fun airportToSearchResultTapped(itemId: String, index: Int) {
        itemStore.update(itemId) { data ->
            val selected = data.airportToSearchResults.getOrNull(index)
            data.copy(
                airportTo = selected,
                arrival = selected?.timeZone?.let { data.arrival?.update(timeZone = it.toZoneId()) }
                    ?: data.arrival,
                airportToSearchResults = emptyList(),
            )
        }
    }

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
                segment.departure,
                segment.airportFrom,
                segment.airportTo,
                segment.arrival
            )
            itemStore.addItem(data, params)
        }
    }

    private fun createItem(
        data: PendingFlight,
        stateParams: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        val minArrival = (data.airportTo?.let { data.departure.atTimeZone(it.timeZone) }
            ?: data.departure) + 1.hours
        return AddFlightItemState(
            id = data.id,
            timestamp = data.departure,
            startState = ManualAddPlanState(
                time = data.departure,
                minTime = Time.now().toMidnight(),
                dateSelectionEnabled = stateParams.dateSelectionEnabled,
                locationText = data.airportFrom?.name,
                searchResults = data.airportFromSearchResults.map { it.name },
            ),
            endState = ManualAddPlanState(
                time = data.arrival?.takeIf { it >= minArrival },
                minTime = minArrival,
                dateSelectionEnabled = true,
                locationText = data.airportTo?.name,
                searchResults = data.airportToSearchResults.map { it.name },
            ),
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            deleteButtonEnabled = stateParams.deleteEnabled,
            saveButtonEnabled = data.arrival?.let { it >= minArrival } ?: false && data.airportFrom != null && data.airportTo != null,
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

    private fun PendingFlight.minArrival(
        departureTime: Time = this.departure
    ) = (airportTo?.let { departureTime.atTimeZone(it.timeZone) } ?: departureTime) + 1.hours
}
