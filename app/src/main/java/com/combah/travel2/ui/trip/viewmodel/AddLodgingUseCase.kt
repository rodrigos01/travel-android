package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingLodging
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AddLodgingUseCase private constructor(
    private val itemStore: AddPlanUseCase.ItemStore,
    private val repository: AddLodgingRepository,
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState, PendingLodging>,
    AddLodgingItemActionHandler {

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
        val results = repository.autocomplete(content.toString())
        itemStore.update(itemId) { data ->
            data.copy(
                searchResults = results
            )
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