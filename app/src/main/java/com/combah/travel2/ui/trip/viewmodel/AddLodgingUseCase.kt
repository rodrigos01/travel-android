package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseStore
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AddLodgingUseCase private constructor(
    private val itemStore: AddPlanItemStore<Lodging, PendingLodging, AddLodgingItemState>,
    private val inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState>,
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState>,
    AddPlanUseCase.ItemStore<Lodging, AddLodgingItemState> by itemStore,
    AddLodgingItemActionHandler {

    constructor(
        repository: AddLodgingRepository,
        timeFormatter: TimeFormatter,
        itemStoreFactory: AddPlanItemStore.Factory<Lodging, PendingLodging, AddLodgingItemState> = AddPlanItemStore.Factory(),
        inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState> = InputUseCaseStore.Factory<InputUseCaseSet, InputState>()
            .create(setFactory = {
                InputUseCaseSet(
                    it.createAutoCompleteUseCase(
                        repository
                    )
                )
            }),
    ) : this(
        itemStoreFactory.create(
            dataFactory = DataFactory(),
            itemFactory = ItemFactory(timeFormatter, inputUseCaseStore),
        ),
        inputUseCaseStore,
    )

    data class PendingLodging(
        override val id: String,
        val checkIn: Time,
        val checkOut: Time,
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
    ) : AddPlanItemStore.AddPlanData

    class InputUseCaseSet(
        val lodgingAutoCompleteUseCase: AutoCompleteUseCase<SimplePlace>,
    ) : InputUseCaseStore.UseCaseSet<InputState> {

        override val state: Flow<InputState> = lodgingAutoCompleteUseCase.state.map {
            InputState(lodgingSearchResults = it.searchResults)
        }
    }

    data class InputState(
        val lodgingSearchResults: List<SimplePlace>,
    )

    override val items: Flow<Map<String, AddLodgingItemState>>
        get() = combine(itemStore.items, inputUseCaseStore.inputStates) { itemMap, inputStateMap ->
            itemMap.entries.associate { (key, value) ->
                key to value.copy(startState = value.startState.copy(searchResults = inputStateMap[key]?.lodgingSearchResults?.map {
                    it.name ?: it.address
                } ?: emptyList()))
            }
        }

    private class DataFactory : AddPlanItemStore.DataFactory<Lodging, PendingLodging> {

        override fun createData(time: Time) = PendingLodging(
            id = UUID.randomUUID().toString(),
            checkIn = time,
            checkOut = time.toMidnight() + 1.days,
        )

        override fun createData(entity: Lodging): PendingLodging = PendingLodging(
            id = entity.id,
            name = entity.name,
            address = entity.address,
            checkIn = entity.checkIn,
            checkOut = entity.checkout
        )

    }

    private class ItemFactory(
        private val timeFormatter: TimeFormatter,
        private val inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState>,
    ) : AddPlanItemStore.ItemFactory<AddLodgingItemState, PendingLodging> {

        override fun createItem(
            data: PendingLodging,
            dateSelectionEnabled: Boolean,
            typeSelectionEnabled: Boolean,
            deleteEnabled: Boolean,
        ): AddLodgingItemState {
            return AddLodgingItemState(
                id = data.id,
                timestamp = data.checkIn,
                startState = ManualAddPlanState(
                    time = data.checkIn,
                    minTime = data.checkIn.toMidnight(),
                    dateSelectionEnabled = dateSelectionEnabled,
                    locationText = data.name ?: data.address,
                    searchResults = emptyList(),
                ),
                endState = ManualAddPlanState(
                    time = data.checkOut,
                    minTime = data.checkIn.toMidnight() + 1.days,
                    dateSelectionEnabled = true,
                    locationText = null,
                    searchResults = emptyList(),
                ),
                saveButtonEnabled = data.checkOut > data.checkIn && (data.name
                    ?: data.address) != null,
                deleteButtonEnabled = deleteEnabled,
                typeSelectionEnabled = typeSelectionEnabled,
            ).also {
                inputUseCaseStore.register(it.id)
            }
        }
    }

    override fun setCheckInDate(itemId: String, date: Time) = itemStore.update(itemId) {
        it.copy(
            checkIn = it.checkIn.update(
                dayOfMonth = date.dayOfMonth,
                month = date.month,
                year = date.year,
            )
        )
    }

    override fun setCheckInTime(itemId: String, hour: Int, minute: Int) {
        itemStore.update(itemId) {
            it.copy(
                checkIn = it.checkIn.update(hour = hour, minute = minute)
            )
        }
    }

    override fun setCheckOutDate(itemId: String, date: Time) {
        itemStore.update(itemId) {
            it.copy(
                checkOut = it.checkOut.update(
                    dayOfMonth = date.dayOfMonth,
                    month = date.month,
                    year = date.year,
                )
            )
        }
    }

    override fun setCheckoutTime(itemId: String, hour: Int, minute: Int) {
        itemStore.update(itemId) {
            it.copy(
                checkOut = it.checkOut.update(hour = hour, minute = minute)
            )
        }
    }

    override suspend fun lodgingTextChanged(itemId: String, content: CharSequence) {
        inputUseCaseStore.get(itemId)?.lodgingAutoCompleteUseCase?.setQuery(content.toString())
    }

    override fun lodgingSearchResultTapped(itemId: String, index: Int) {
        val useCase = inputUseCaseStore.get(itemId)?.lodgingAutoCompleteUseCase
        val selected = useCase?.state?.value?.searchResults?.getOrNull(index)
        useCase?.clearResults()
        itemStore.update(itemId) {
            selected?.let { selected ->
                it.copy(
                    name = selected.name,
                    address = selected.address,
                    city = selected.city,
                )
            } ?: it
        }
    }

    override fun createAppData(item: AddLodgingItemState): Lodging {
        val pending = itemStore.get(item.id) ?: error("provided Id is not from this Use Case")
        pending.address ?: error("address from is not set")
        pending.city ?: error("city from is not set")
        pending.checkOut
        return Lodging(
            item.id,
            item.startState.locationText,
            pending.address,
            pending.city,
            pending.checkIn,
            pending.checkOut,
        )
    }
}
