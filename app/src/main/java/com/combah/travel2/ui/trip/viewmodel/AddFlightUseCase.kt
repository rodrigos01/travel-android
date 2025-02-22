package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.filterValueInstanceOf
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingFlight
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.minutes

class AddFlightUseCase private constructor(
    private val itemStore: AddPlanUseCase.ItemStore,
    private val airportFromAutoCompleteUseCase: AutoCompleteUseCase<Airport>,
    private val airportToAutoCompleteUseCase: AutoCompleteUseCase<Airport>,
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightItemState, PendingFlight>,
    AddFlightItemActionHandler {

    constructor(itemStore: AddPlanUseCase.ItemStore, repository: AddFlightRepository) : this(
        itemStore,
        AutoCompleteUseCase(repository),
        AutoCompleteUseCase(repository),
    )

    private val storeItems: Flow<Map<String, AddFlightItemState>> =
        itemStore.items.filterValueInstanceOf()
    val items: Flow<Map<String, AddFlightItemState>> =
        combine(
            storeItems,
            airportFromAutoCompleteUseCase.state,
            airportToAutoCompleteUseCase.state
        ) { itemMap, airportFromInputState, airportToInputState ->
            itemMap.entries.associate { (id, item) ->
                id to item.copy(
                    startState = item.startState.copy(searchResults = airportFromInputState[id]?.searchResults?.map { it.name }
                        ?: emptyList()),
                    endState = item.endState.copy(searchResults = airportToInputState[id]?.searchResults?.map { it.name }
                        ?: emptyList()),
                )
            }
        }

    override fun setDepartureDate(itemId: String, date: Time) {
        itemStore.update(itemId) {
            it.copy(
                departure = it.departure.update(
                    dayOfMonth = date.dayOfMonth,
                    month = date.month,
                    year = date.year,
                )
            )
        }
    }

    override fun setDepartureTime(itemId: String, hour: Int, minute: Int) {
        itemStore.update(itemId) {
            it.copy(departure = it.departure.update(hour = hour, minute = minute))
        }
    }

    override fun setArrivalDate(itemId: String, date: Time) {
        itemStore.update(itemId) {
            it.copy(
                arrival = (it.arrival ?: it.departure).update(
                    dayOfMonth = date.dayOfMonth,
                    month = date.month,
                    year = date.year,
                )
            )
        }
    }

    override fun setArrivalTime(itemId: String, hour: Int, minute: Int) {
        itemStore.update(itemId) {
            val baseTime = it.arrival ?: it.departure
            it.copy(
                arrival = baseTime.update(
                    hour = hour,
                    minute = minute,
                    timeZone = it.airportTo?.timeZone ?: baseTime.timeZone,
                )
            )
        }
    }

    override suspend fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    ) {
        airportFromAutoCompleteUseCase.setQuery(itemId, content.toString())
    }

    override fun airportFromSearchResultTapped(itemId: String, index: Int) {
        val selected = airportSearchResultTapped(airportFromAutoCompleteUseCase, itemId, index)
        itemStore.update(itemId) {
            it.copy(
                airportFrom = selected
            )
        }
    }

    override suspend fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    ) {
        airportToAutoCompleteUseCase.setQuery(itemId, content.toString())
    }

    override fun airportToSearchResultTapped(itemId: String, index: Int) {
        val selected = airportSearchResultTapped(airportToAutoCompleteUseCase, itemId, index)
        itemStore.update(itemId) { data ->
            data.copy(
                airportTo = selected,
                arrival = selected?.timeZone?.let { data.arrival?.update(timeZone = it) }
                    ?: data.arrival
            )
        }
    }

    private fun airportSearchResultTapped(
        useCase: AutoCompleteUseCase<Airport>,
        itemId: String,
        index: Int,
    ): Airport? {
        val selected = useCase.state[itemId]?.searchResults?.getOrNull(index)
        useCase.clearResults(itemId)
        return selected
    }

    override fun createData(time: Time): PendingFlight = PendingFlight(
        id = UUID.randomUUID().toString(),
        departure = time,
    )

    override fun createData(entity: Flight): PendingFlight {
        val segment = entity.segments.firstOrNull()
        val data = PendingFlight(
            entity.id,
            segment?.departure ?: Time.now(),
            segment?.airportFrom,
            segment?.airportTo,
            segment?.arrival
        )
        return data
    }

    override fun createItem(
        data: PendingFlight,
        dateSelectionEnabled: Boolean,
        typeSelectionEnabled: Boolean,
        deleteEnabled: Boolean,
    ): AddFlightItemState {
        val arrivalTime = data.arrival ?: data.departure
        val item = AddFlightItemState(
            id = data.id,
            timestamp = data.departure,
            startState = ManualAddPlanState(
                time = data.departure,
                minTime = Time.now().toMidnight(),
                dateSelectionEnabled = dateSelectionEnabled,
                locationText = data.airportFrom?.name,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                time = arrivalTime,
                minTime = Time(
                    data.departure.timeInMillis,
                    timeZone = data.airportTo?.timeZone ?: data.departure.timeZone,
                ) + 1.minutes,
                dateSelectionEnabled = true,
                locationText = data.airportTo?.name,
                searchResults = emptyList(),
            ),
            typeSelectionEnabled = typeSelectionEnabled,
            deleteButtonEnabled = deleteEnabled,
            saveButtonEnabled = data.arrival?.let { it > data.departure } ?: false && data.airportFrom != null && data.airportTo != null,
        )
        return item
    }

    override fun createAppData(data: PendingFlight): Flight {
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

    private fun AddPlanUseCase.ItemStore.update(
        itemId: String,
        updater: (PendingFlight) -> PendingFlight
    ) = update(itemId, this@AddFlightUseCase, updater)
}
