package travel.vola.android.ui.trip.viewmodel

import com.vola.android.common.coroutines.MutexScope
import com.vola.android.extensions.MapFlow
import com.vola.android.extensions.plus
import com.vola.android.extensions.toMidnight
import com.vola.android.model.PlaceRepository
import com.vola.android.model.data.Lodging
import com.vola.android.model.data.Time
import com.vola.android.model.repository.LodgingSearchRepository
import com.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import com.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import com.vola.android.ui.trip.creation.usecase.LodgingSearchItemActionHandler
import com.vola.android.ui.trip.creation.usecase.PendingData
import com.vola.android.ui.trip.state.LodgingSearchItemState
import com.vola.android.ui.trip.state.SearchResultItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

interface LodgingSearchParamsFactory {
    fun getLodgingSearchParams(tripId: String, itemId: String): LodgingSearchDestination.Params?
}

class LodgingSearchParamsUseCase(
    coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingData.LodgingSearchParams, LodgingSearchItemState> = AddPlanItemStore(),
    private val repository: LodgingSearchRepository = LodgingSearchRepository(),
    private val placeRepository: PlaceRepository,
) : AddPlanUseCase.AddItemUseCase<Lodging, LodgingSearchItemState>, LodgingSearchItemActionHandler,
    LodgingSearchParamsFactory {

    override val items: MapFlow<String, LodgingSearchItemState> = itemStore.items(::createItem)

    override fun setCheckInTime(itemId: String, time: Time) {
        itemStore.update(itemId) { it.copy(checkIn = time) }
    }

    override fun setCheckOutTime(itemId: String, time: Time) {
        itemStore.update(itemId) { it.copy(checkOut = time) }
    }

    private val autoCompleteScope = MutexScope(coroutineScope.coroutineContext)
    override fun locationTextChanged(itemId: String, content: CharSequence) {
        if (content.length < 3) {
            return
        }
        autoCompleteScope.launch {
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
    ) {
        val data = PendingData.LodgingSearchParams(
            id = id,
            checkIn = time,
        )
        itemStore.addItem(data, params)
    }

    override fun addItem(
        id: String,
        entity: Lodging,
        params: AddPlanUseCase.StateParams
    ) {
        val data = PendingData.LodgingSearchParams(
            id = id,
            checkIn = entity.checkIn,
            checkOut = entity.checkout,
        )
        itemStore.addItem(data, params)
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

    override fun getLodgingSearchParams(
        tripId: String,
        itemId: String
    ): LodgingSearchDestination.Params? =
        itemStore.getData(itemId)?.let {
            if (it.checkOut != null && it.city != null) {
                placeRepository.places[it.city.id] = it.city
                LodgingSearchDestination.Params(
                    tripId = tripId,
                    checkIn = it.checkIn.timeInMillis,
                    checkOut = it.checkOut.timeInMillis,
                    locationId = it.city.id,
                    timeZoneId = it.checkIn.timeZone.id
                )
            } else {
                null
            }
        }
}
