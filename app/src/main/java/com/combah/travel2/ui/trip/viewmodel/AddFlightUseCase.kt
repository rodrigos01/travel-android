package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.ui.trip.creation.usecase.AddFlightItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseStore
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.minutes

class AddFlightUseCase private constructor(
    private val itemStore: AddPlanItemStore<Flight, PendingFlight, AddFlightItemState>,
    private val inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState>,
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightItemState>,
    AddPlanUseCase.ItemStore<Flight, AddFlightItemState> by itemStore,
    AddFlightItemActionHandler {

    constructor(
        repository: AddFlightRepository,
        timeFormatter: TimeFormatter,
        itemStoreFactory: AddPlanItemStore.Factory<Flight, PendingFlight, AddFlightItemState> = AddPlanItemStore.Factory(),
        inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState> = InputUseCaseStore.Factory<InputUseCaseSet, InputState>()
            .create(setFactory = {
                InputUseCaseSet(
                    airportFromAutoCompleteUseCase = it.createAutoCompleteUseCase(
                        repository
                    ),
                    airportToAutoCompleteUseCase = it.createAutoCompleteUseCase(
                        repository
                    ),
                )
            }),
    ) : this(
        itemStoreFactory.create(
            dataFactory = DataFactory(inputUseCaseStore),
            itemFactory = ItemFactory(timeFormatter),
        ),
        inputUseCaseStore,
    )

    data class PendingFlight(
        override val id: String,
        val departure: Time,
        val airportFrom: Airport? = null,
        val airportTo: Airport? = null,
        val arrival: Time? = null,
    ) : AddPlanItemStore.AddPlanData

    class InputUseCaseSet(
        val airportFromAutoCompleteUseCase: AutoCompleteUseCase<Airport>,
        val airportToAutoCompleteUseCase: AutoCompleteUseCase<Airport>,
    ) : InputUseCaseStore.UseCaseSet<InputState> {

        override val state = combine(
            airportFromAutoCompleteUseCase.state, airportToAutoCompleteUseCase.state
        ) { from, to ->
            InputState(from.searchResults, to.searchResults)
        }
    }

    data class InputState(
        val airportFromSearchResults: List<Airport>,
        val airportToSearchResults: List<Airport>,
    )

    class DataFactory(private val inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState>) :
        AddPlanItemStore.DataFactory<Flight, PendingFlight> {
        override fun createData(time: Time): PendingFlight {
            val data = PendingFlight(
                id = UUID.randomUUID().toString(),
                departure = time,
            )
            inputUseCaseStore.register(data.id)
            return data
        }

        override fun createData(entity: Flight): PendingFlight {
            val segment = entity.segments.firstOrNull()
            val data = PendingFlight(
                entity.id,
                segment?.departure ?: Time.now(),
                segment?.airportFrom,
                segment?.airportTo,
                segment?.arrival
            )
            inputUseCaseStore.register(data.id)
            return data
        }
    }

    class ItemFactory(private val timeFormatter: TimeFormatter) :
        AddPlanItemStore.ItemFactory<AddFlightItemState, PendingFlight> {
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
    }

    override val items: Flow<Map<String, AddFlightItemState>> =
        combine(itemStore.items, inputUseCaseStore.inputStates) { itemMap, inputStateMap ->
            itemMap.map { (id, item) ->
                id to item.copy(
                    startState = item.startState.copy(searchResults = inputStateMap[id]?.airportFromSearchResults?.map { it.name }
                        ?: emptyList()),
                    endState = item.endState.copy(searchResults = inputStateMap[id]?.airportToSearchResults?.map { it.name }
                        ?: emptyList()),
                )
            }.toMap()
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
        inputUseCaseStore.get(itemId)?.airportFromAutoCompleteUseCase?.setQuery(content.toString())
    }

    override fun airportFromSearchResultTapped(itemId: String, index: Int) {
        val useCase = inputUseCaseStore.get(itemId)?.airportFromAutoCompleteUseCase
        val selected = useCase?.state?.value?.searchResults?.getOrNull(index)
        useCase?.clearResults()
        itemStore.update(itemId) {
            it.copy(
                airportFrom = selected
            )
        }
    }

    override suspend fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    ) {
        inputUseCaseStore.get(itemId)?.airportToAutoCompleteUseCase?.setQuery(content.toString())
    }

    override fun airportToSearchResultTapped(itemId: String, index: Int) {
        val useCase = inputUseCaseStore.get(itemId)?.airportToAutoCompleteUseCase
        val selected = useCase?.state?.value?.searchResults?.getOrNull(index)
        useCase?.clearResults()
        itemStore.update(itemId) { data ->
            data.copy(
                airportTo = selected,
                arrival = selected?.timeZone?.let { data.arrival?.update(timeZone = it) }
                    ?: data.arrival
            )
        }
    }

    override fun createAppData(item: AddFlightItemState): Flight {
        val pending = itemStore.get(item.id) ?: error("provided Id is not from this Use Case")
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
}
