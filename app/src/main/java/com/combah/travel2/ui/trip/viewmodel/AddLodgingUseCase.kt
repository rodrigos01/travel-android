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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AddLodgingUseCase private constructor(
    private val itemStore: AddPlanItemStore<Lodging, PendingLodging, AddLodgingItem>,
    private val inputUseCaseStore: InputUseCaseStore<InputUseCaseSet, InputState>,
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingUseCase.AddLodgingItem>,
    AddPlanUseCase.ItemStore<Lodging, AddLodgingUseCase.AddLodgingItem> by itemStore,
    AddLodgingItemActionHandler {

    constructor(
        repository: AddLodgingRepository,
        timeFormatter: TimeFormatter,
        itemStoreFactory: AddPlanItemStore.Factory<Lodging, PendingLodging, AddLodgingItem> = AddPlanItemStore.Factory(),
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

    data class AddLodgingItem(
        override val id: String,
        override val timestamp: Time,
        val minCheckInTime: Time,
        val checkInDayOfMonth: String,
        val checkInDayOfWeek: String,
        val name: String? = null,
        val lodgingSearchResults: List<String> = emptyList(),
        val checkInTime: String? = null,
        val minCheckOutTime: Time,
        val checkOutDayOfMonth: String,
        val checkOutDayOfWeek: String,
        val checkOutTime: String? = null,
        override val saveButtonEnabled: Boolean,
        override val startDateSelectionEnabled: Boolean = false,
    ) : AddPlanUseCase.AddPlanItem

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

    override val items: Flow<Map<String, AddLodgingItem>>
        get() = combine(itemStore.items, inputUseCaseStore.inputStates) { itemMap, inputStateMap ->
            itemMap.entries.associate { (key, value) ->
                key to value.copy(lodgingSearchResults = inputStateMap[key]?.lodgingSearchResults?.map {
                    it.name ?: it.address
                } ?: emptyList())
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
    ) : AddPlanItemStore.ItemFactory<AddLodgingItem, PendingLodging> {

        override fun createItem(
            data: PendingLodging,
            startDateSelectionEnabled: Boolean,
        ): AddLodgingItem {
            return AddLodgingItem(
                id = data.id,
                timestamp = data.checkIn,
                minCheckInTime = data.checkIn.toMidnight(),
                checkInDayOfWeek = timeFormatter.dayOfWeekString(data.checkIn),
                checkInDayOfMonth = timeFormatter.dayOfMonthString(data.checkIn),
                name = data.name ?: data.address,
                checkInTime = timeFormatter.timeString(data.checkIn),
                minCheckOutTime = data.checkIn.toMidnight() + 1.days,
                checkOutDayOfMonth = timeFormatter.dayOfMonthString(data.checkOut),
                checkOutDayOfWeek = timeFormatter.dayOfWeekString(data.checkOut),
                checkOutTime = timeFormatter.timeString(data.checkOut),
                saveButtonEnabled = data.checkOut > data.checkIn && (data.name
                    ?: data.address) != null,
                startDateSelectionEnabled = startDateSelectionEnabled,
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

    override fun createAppData(item: AddLodgingItem): Lodging {
        val pending = itemStore.get(item.id) ?: error("provided Id is not from this Use Case")
        pending.address ?: error("address from is not set")
        pending.city ?: error("city from is not set")
        pending.checkOut
        return Lodging(
            item.id,
            item.name,
            pending.address,
            pending.city,
            pending.checkIn,
            pending.checkOut,
        )
    }
}
