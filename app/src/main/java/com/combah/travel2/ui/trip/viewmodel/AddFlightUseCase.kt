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
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseFactory
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2

class AddFlightUseCase(
    private val addFlightRepository: AddFlightRepository,
    private val timeFormatter: TimeFormatter,
    itemStoreFactory: AddPlanItemStore.Factory<PendingFlight, AddFlightItem> = AddPlanItemStore.Factory(),
    inputUseCaseStoreFactory: InputUseCaseStore.Factory<InputUseCaseSet, InputState> = InputUseCaseStore.Factory()
) : AddPlanUseCase.AddItemUseCase<Flight, AddFlightUseCase.AddFlightItem>,
    AddFlightItemActionHandler, AddPlanItemStore.DataFactory<AddFlightUseCase.PendingFlight>,
    AddPlanItemStore.ItemFactory<AddFlightUseCase.AddFlightItem, AddFlightUseCase.PendingFlight>,
    InputUseCaseStore.UseCaseSetFactory<AddFlightUseCase.InputUseCaseSet, AddFlightUseCase.InputState> {

    data class AddFlightItem(
        override val id: String,
        override val timestamp: Time,
        val minDepartureTime: Time,
        val minArrivalTime: Time,
        val departureTime: String? = null,
        val departureDayOfMonth: String,
        val departureDayOfWeek: String,
        val airportFromName: String? = null,
        val airportFromSearchResults: List<String> = emptyList(),
        val arrivalTime: String? = null,
        val arrivalDayOfMonth: String,
        val arrivalDayOfWeek: String,
        val airportToName: String? = null,
        val airportToSearchResults: List<String> = emptyList(),
        override val saveButtonEnabled: Boolean,
        override val startDateSelectionEnabled: Boolean = false,
    ) : AddPlanUseCase.AddPlanItem

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

    private val itemStore: AddPlanItemStore<PendingFlight, AddFlightItem> = itemStoreFactory.create(
        dataFactory = this,
        itemFactory = this,
    )

    private val inputUseCaseStore = inputUseCaseStoreFactory.create(setFactory = this)

    override val items: Flow<Map<String, AddFlightItem>> =
        combine(itemStore.items, inputUseCaseStore.inputStates) { itemMap, inputStateMap ->
            itemMap.map { (id, item) ->
                id to item.copy(
                    airportFromSearchResults = inputStateMap[id]?.airportFromSearchResults?.map { it.name }
                        ?: emptyList(),
                    airportToSearchResults = inputStateMap[id]?.airportToSearchResults?.map { it.name }
                        ?: emptyList(),
                )
            }.toMap()
        }

    override fun createData(time: Time): PendingFlight {
        val data = PendingFlight(
            id = UUID.randomUUID().toString(),
            departure = time,
        )
        inputUseCaseStore.register(data.id)
        return data
    }

    override fun createItem(
        data: PendingFlight,
        startDateSelectionEnabled: Boolean,
    ): AddFlightItem {
        val arrivalTime = data.arrival ?: data.departure
        val item = AddFlightItem(
            id = data.id,
            timestamp = data.departure,
            minDepartureTime = Time.now().toMidnight(),
            minArrivalTime = if (data.arrival?.toMidnight() == data.departure.toMidnight()) data.departure else data.departure.toMidnight(),
            departureTime = timeFormatter.timeString(data.departure),
            departureDayOfWeek = timeFormatter.dayOfWeekString(data.departure),
            departureDayOfMonth = timeFormatter.dayOfMonthString(data.departure),
            airportFromName = data.airportFrom?.name,
            arrivalDayOfWeek = timeFormatter.dayOfWeekString(arrivalTime),
            arrivalDayOfMonth = timeFormatter.dayOfMonthString(arrivalTime),
            arrivalTime = data.arrival?.let { timeFormatter.timeString(it) },
            airportToName = data.airportTo?.name,
            saveButtonEnabled = data.arrival?.let { it > data.departure } ?: false && data.airportFrom != null && data.airportTo != null,
            startDateSelectionEnabled = startDateSelectionEnabled,
        )
        return item
    }

    override fun addItem(time: Time, startDateSelectionEnabled: Boolean): AddFlightItem {
        return itemStore.addItem(time, startDateSelectionEnabled)
    }

    override fun remove(item: AddFlightItem) {
        itemStore.remove(item)
    }

    override fun createUseCaseSet(useCaseFactory: InputUseCaseFactory): InputUseCaseSet {
        return InputUseCaseSet(
            airportFromAutoCompleteUseCase = useCaseFactory.createAutoCompleteUseCase(
                addFlightRepository
            ),
            airportToAutoCompleteUseCase = useCaseFactory.createAutoCompleteUseCase(
                addFlightRepository
            ),
        )
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

    override fun createAppData(item: AddFlightItem): Flight {
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
