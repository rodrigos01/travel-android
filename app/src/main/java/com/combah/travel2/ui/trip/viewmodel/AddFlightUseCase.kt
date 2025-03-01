package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingFlight
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

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
                )
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
            val selected = it.airportToSearchResults.getOrNull(index)
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
                arrival = selected?.timeZone?.let { data.arrival?.update(timeZone = it) }
                    ?: data.arrival,
                airportToSearchResults = emptyList(),
            )
        }
    }

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        val data = PendingFlight(
            id = id,
            departure = time,
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    override fun addItem(entity: Flight, params: AddPlanUseCase.StateParams): AddFlightItemState {
        val segment = entity.segments.firstOrNull()
        val data = PendingFlight(
            entity.id,
            segment?.departure ?: Time.now(),
            segment?.airportFrom,
            segment?.airportTo,
            segment?.arrival
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    private fun createItem(
        data: PendingFlight,
        stateParams: AddPlanUseCase.StateParams,
    ): AddFlightItemState {
        val arrivalTime = data.arrival ?: data.departure
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
                time = arrivalTime,
                minTime = Time(
                    data.departure.timeInMillis,
                    timeZone = data.airportTo?.timeZone ?: data.departure.timeZone,
                ) + 1.minutes,
                dateSelectionEnabled = true,
                locationText = data.airportTo?.name,
                searchResults = data.airportToSearchResults.map { it.name },
            ),
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            deleteButtonEnabled = stateParams.deleteEnabled,
            saveButtonEnabled = data.arrival?.let { it > data.departure } ?: false && data.airportFrom != null && data.airportTo != null,
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
            data.id,
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
}
