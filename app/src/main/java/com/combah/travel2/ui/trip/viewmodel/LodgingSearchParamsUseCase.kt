package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.lodgingsearch.composable.LodgingSearchDestination
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.LodgingSearchItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.PendingData
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.LodgingSearchItemState
import com.combah.travel2.ui.trip.state.SearchResultItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

class LodgingSearchParamsUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingData.LodgingSearchParams, LodgingSearchItemState> = AddPlanItemStore(),
    private val repository: AddLodgingRepository = AddLodgingRepository(),
) : AddPlanUseCase.AddItemUseCase<Lodging, LodgingSearchItemState>, LodgingSearchItemActionHandler {

    override val items: MapFlow<String, LodgingSearchItemState> = itemStore.items(::createItem)

    override fun setCheckInTime(itemId: String, time: Time) {
        itemStore.update(itemId) { it.copy(checkIn = time) }
    }

    override fun setCheckOutTime(itemId: String, time: Time) {
        itemStore.update(itemId) { it.copy(checkOut = time) }
    }

    override fun locationTextChanged(itemId: String, content: CharSequence) {
        if (content.length < 3) {
            return
        }
        coroutineScope.launch {
            val results = repository.autocompleteCity(content.toString())
            itemStore.update(itemId) { data ->
                data.copy(
                    searchResults = results
                )
            }
        }
    }

    override fun locationSearchResultTapped(itemId: String, index: Int) {
        itemStore.update(itemId) { it.copy(city = it.searchResults[index]) }
    }

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ): AddPlanItemState {
        val data = PendingData.LodgingSearchParams(
            id = id,
            checkIn = time,
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    override fun addItem(
        entity: Lodging,
        params: AddPlanUseCase.StateParams
    ): LodgingSearchItemState {
        val data = PendingData.LodgingSearchParams(
            id = entity.id,
            checkIn = entity.checkIn,
            checkOut = entity.checkout,
        )
        itemStore.addItem(data, params)
        return createItem(data, params)
    }

    override fun removeItem(item: LodgingSearchItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        searchParams: PendingData.LodgingSearchParams,
        stateParams: AddPlanUseCase.StateParams
    ) =
        LodgingSearchItemState(
            id = searchParams.id,
            timestamp = searchParams.checkIn,
            saveButtonEnabled = searchParams.city != null && searchParams.checkOut != null && searchParams.checkOut > searchParams.checkIn,
            dateSelectionEnabled = stateParams.dateSelectionEnabled,
            deleteButtonEnabled = stateParams.deleteEnabled,
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            checkIn = searchParams.checkIn,
            minCheckOutTime = searchParams.checkIn.toMidnight() + 1.days,
            checkOut = searchParams.checkOut,
            locationText = searchParams.city?.name,
            searchResults = searchParams.searchResults.map {
                SearchResultItemState(
                    it.name,
                    it.address
                )
            },
        )

    fun getParams(itemId: String): LodgingSearchDestination.Params? =
        itemStore.getData(itemId)?.let {
            if (it.checkOut != null && it.city != null) {
                LodgingSearchDestination.Params(
                    checkIn = it.checkIn.timeInMillis,
                    checkOut = it.checkOut.timeInMillis,
                    locationId = it.city.id,
                    locationName = it.city.name,
                    timeZoneId = it.checkIn.timeZone.id
                )
            } else {
                null
            }
        }
}
