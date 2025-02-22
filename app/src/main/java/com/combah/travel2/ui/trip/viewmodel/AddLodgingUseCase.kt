package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.filterValueInstanceOf
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingLodging
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AddLodgingUseCase private constructor(
    private val itemStore: AddPlanUseCase.ItemStore,
    private val autoCompleteUseCase: AutoCompleteUseCase<SimplePlace>,
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState, PendingLodging>,
    AddLodgingItemActionHandler {

    constructor(
        itemStore: AddPlanUseCase.ItemStore,
        repository: AddLodgingRepository
    ) : this(
        itemStore,
        AutoCompleteUseCase(repository),
    )

    private val storeItems: Flow<Map<String, AddLodgingItemState>> =
        itemStore.items.filterValueInstanceOf()
    val items: Flow<Map<String, AddLodgingItemState>>
        get() = combine(storeItems, autoCompleteUseCase.state) { itemMap, inputStateMap ->
            itemMap.entries.associate { (key, value) ->
                key to value.copy(startState = value.startState.copy(searchResults = inputStateMap[key]?.searchResults?.map {
                    it.name ?: it.address
                } ?: emptyList()))
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
        autoCompleteUseCase.setQuery(itemId, content.toString())
    }

    override fun lodgingSearchResultTapped(itemId: String, index: Int) {
        val selected = autoCompleteUseCase.state[itemId]?.searchResults?.getOrNull(index)
        autoCompleteUseCase.clearResults(itemId)
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
        )
    }

    override fun createAppData(data: PendingLodging): Lodging {
        val item = itemStore.getItem(data.id) as? AddLodgingItemState
            ?: error("PendingLodging has no state item associated to it")
        data.address ?: error("address from is not set")
        data.city ?: error("city from is not set")
        data.checkOut
        return Lodging(
            item.id,
            item.startState.locationText,
            data.address,
            data.city,
            data.checkIn,
            data.checkOut,
        )
    }

    private fun AddPlanUseCase.ItemStore.update(
        itemId: String,
        updater: (PendingLodging) -> PendingLodging
    ) = update(itemId, this@AddLodgingUseCase, updater)
}