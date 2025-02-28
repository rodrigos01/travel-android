package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapFlow
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.model.repository.AddLodgingRepository.ResultType
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.LodgingSearchItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.PendingData
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.LodgingSearchItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
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
        coroutineScope.launch {
            val results = repository.autocomplete(content.toString(), ResultType.City)
            itemStore.update(itemId) { data ->
                data.copy(
                    searchResults = results
                )
            }
        }
    }

    override fun locationSearchResultTapped(itemId: String, index: Int) {
        itemStore.update(itemId) { it.copy(city = it.searchResults[index].city) }
    }

    override fun addItem(time: Time, params: AddPlanUseCase.StateParams): AddPlanItemState {
        val data = PendingData.LodgingSearchParams(
            id = UUID.randomUUID().toString(),
            checkIn = time,
            checkOut = time.toMidnight() + 1.days,
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
            saveButtonEnabled = searchParams.city != null && searchParams.checkOut > searchParams.checkIn,
            dateSelectionEnabled = stateParams.dateSelectionEnabled,
            deleteButtonEnabled = stateParams.deleteEnabled,
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            checkIn = searchParams.checkIn,
            checkOut = searchParams.checkOut,
            locationText = searchParams.city?.name,
            searchResults = searchParams.searchResults.map { it.name ?: it.address },
        )
}
