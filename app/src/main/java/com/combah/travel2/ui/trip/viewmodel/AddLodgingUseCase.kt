package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingLodging
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AddLodgingUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingLodging, AddLodgingItemState> = AddPlanItemStore(),
    private val repository: AddLodgingRepository = AddLodgingRepository(),
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState>,
    AddLodgingItemActionHandler {

    override val items: MapFlow<String, AddLodgingItemState> = itemStore.items(::createItem)

    override fun setCheckInTime(itemId: String, time: Time) = itemStore.update(itemId) {
        it.copy(
            checkIn = it.checkIn.update(
                dayOfMonth = time.dayOfMonth,
                month = time.month,
                year = time.year,
                hour = time.hour,
                minute = time.minute,
            )
        )
    }

    override fun setCheckOutTime(itemId: String, time: Time) {
        itemStore.update(itemId) {
            it.copy(
                checkOut = it.checkOut.update(
                    dayOfMonth = time.dayOfMonth,
                    month = time.month,
                    year = time.year,
                    hour = time.hour,
                    minute = time.minute,
                )
            )
        }
    }

    override fun lodgingTextChanged(itemId: String, content: CharSequence) {
        coroutineScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) { data ->
                data.copy(
                    searchResults = results
                )
            }
        }
    }

    override fun lodgingSearchResultTapped(itemId: String, index: Int) {
        itemStore.update(itemId) { data ->
            val selected = data.searchResults.getOrNull(index)
            data.copy(
                name = selected?.name,
                address = selected?.address,
                city = selected?.city,
                searchResults = emptyList(),
            )
        }
    }

    override fun addItem(time: Time, params: AddPlanUseCase.StateParams): AddLodgingItemState {
        val data = PendingLodging(
            id = UUID.randomUUID().toString(),
            checkIn = time,
            checkOut = time.toMidnight() + 1.days,
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    override fun addItem(
        entity: Lodging,
        params: AddPlanUseCase.StateParams,
    ): AddLodgingItemState {
        val data = PendingLodging(
            id = entity.id,
            name = entity.name,
            address = entity.address,
            checkIn = entity.checkIn,
            checkOut = entity.checkout
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    private fun createItem(
        data: PendingLodging,
        stateParams: AddPlanUseCase.StateParams,
    ): AddLodgingItemState {
        return AddLodgingItemState(
            id = data.id,
            timestamp = data.checkIn,
            startState = ManualAddPlanState(
                time = data.checkIn,
                minTime = data.checkIn.toMidnight(),
                dateSelectionEnabled = stateParams.dateSelectionEnabled,
                locationText = data.name ?: data.address,
                searchResults = data.searchResults.map { it.name ?: it.address },
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
            deleteButtonEnabled = stateParams.deleteEnabled,
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
        )
    }

    override fun removeItem(item: AddLodgingItemState) {
        itemStore.remove(item)
    }

    override fun createEntity(item: AddLodgingItemState): Lodging {
        val data = itemStore.getData(item.id)
            ?: error("item has no pending data associated with it")
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
}
