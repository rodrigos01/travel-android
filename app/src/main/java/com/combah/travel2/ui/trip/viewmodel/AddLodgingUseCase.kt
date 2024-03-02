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
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseFactory
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit

class AddLodgingUseCase(
    private val repository: AddLodgingRepository,
    private val timeFormatter: TimeFormatter,
    itemStoreFactory: AddPlanItemStore.Factory<PendingLodging, AddLodgingItem> = AddPlanItemStore.Factory(),
    inputUseCaseStoreFactory: InputUseCaseStore.Factory<InputUseCaseSet, InputState> = InputUseCaseStore.Factory()
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingUseCase.AddLodgingItem>,
    AddLodgingItemActionHandler, AddPlanItemStore.DataFactory<AddLodgingUseCase.PendingLodging>,
    AddPlanItemStore.ItemFactory<AddLodgingUseCase.AddLodgingItem, AddLodgingUseCase.PendingLodging>,
    InputUseCaseStore.UseCaseSetFactory<AddLodgingUseCase.InputUseCaseSet, AddLodgingUseCase.InputState> {

    data class AddLodgingItem(
        override val id: String,
        override val timestamp: Time,
        val name: String? = null,
        val lodgingSearchResults: List<String> = emptyList(),
        val checkInTime: String? = null,
        val minCheckOutTimeMillis: Long,
        val checkOutDayOfMonth: String,
        val checkOutDayOfWeek: String,
        val checkOutTime: String? = null,
    ) : AddPlanUseCase.AddPlanItem

    data class PendingLodging(
        override val id: String,
        val checkIn: Time,
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val checkOut: Time,
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

    private val itemStore: AddPlanItemStore<PendingLodging, AddLodgingItem> =
        itemStoreFactory.create(
            dataFactory = this,
            itemFactory = this,
        )

    private val inputUseCaseStore = inputUseCaseStoreFactory.create(setFactory = this)
    override val items: Flow<Map<String, AddLodgingItem>>
        get() = combine(itemStore.items, inputUseCaseStore.inputStates) { itemMap, inputStateMap ->
            itemMap.entries.associate { (key, value) ->
                key to value.copy(lodgingSearchResults = inputStateMap[key]?.lodgingSearchResults?.map {
                    it.name ?: it.address
                } ?: emptyList())
            }
        }


    override fun createData(time: Time) = PendingLodging(
        id = UUID.randomUUID().toString(),
        checkIn = time,
        checkOut = time.toMidnight() + TimeUnit.DAYS.toMillis(1)
    )

    override fun createItem(data: PendingLodging): AddLodgingItem = AddLodgingItem(
        id = data.id,
        timestamp = data.checkIn,
        name = data.name ?: data.address,
        checkInTime = timeFormatter.timeString(data.checkIn),
        minCheckOutTimeMillis = data.checkIn.timeInMillis,
        checkOutDayOfMonth = timeFormatter.dayOfMonthString(data.checkOut),
        checkOutDayOfWeek = timeFormatter.dayOfWeekString(data.checkOut),
        checkOutTime = timeFormatter.timeString(data.checkOut),
    ).also {
        inputUseCaseStore.register(it.id)
    }

    override fun addItem(time: Time) = itemStore.addItem(time)

    override fun remove(item: AddLodgingItem) {
        itemStore.remove(item)
    }

    override fun createUseCaseSet(useCaseFactory: InputUseCaseFactory) = InputUseCaseSet(
        useCaseFactory.createAutoCompleteUseCase(repository)
    )

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
        val useCase = inputUseCaseStore.get(itemId)
            ?.lodgingAutoCompleteUseCase
        val selected = useCase?.state
            ?.value?.searchResults?.getOrNull(index)
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
            item.name,
            pending.address,
            pending.city,
            pending.checkIn,
            pending.checkOut,
        )
    }
}
