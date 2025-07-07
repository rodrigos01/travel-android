package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.timeInMillis
import travel.vola.android.extensions.toMidnight
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.LodgingSearchItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.SearchResultItemState
import java.time.ZonedDateTime
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
    override fun lodgingTextChanged(itemId: String, content: CharSequence) {
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

    override fun lodgingSearchResultTapped(itemId: String, index: Int) {
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
        params: AddPlanUseCase.StateParams,
    ) {
        addItem(
            id = id,
            checkIn = entity.checkIn,
            checkOut = entity.checkout,
            city = null, // TODO: Use city from entity when Unified Places API is available
            params = params
        )
    }

    fun addItem(
        id: String,
        checkIn: ZonedDateTime,
        checkOut: ZonedDateTime?,
        city: Place?,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingData.LodgingSearchParams(
            id = id,
            checkIn = checkIn,
            checkOut = checkOut,
            city = city,
        )
        itemStore.addItem(data, params)
    }

    override fun removeItem(item: LodgingSearchItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        searchParams: PendingData.LodgingSearchParams,
        stateParams: AddPlanUseCase.StateParams,
    ) = LodgingSearchItemState(
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
                it.name, it.address
            )
        },
    )

    override fun getLodgingSearchParams(
        tripId: String,
        itemId: String,
    ): LodgingSearchDestination.Params? = itemStore.getData(itemId)?.let {
        if (it.checkOut != null && it.city != null) {
            placeRepository.places[it.city.id] = it.city
            LodgingSearchDestination.Params(
                tripId = tripId,
                checkIn = it.checkIn.timeInMillis,
                checkOut = it.checkOut.timeInMillis,
                locationId = it.city.id,
                timeZoneId = it.checkIn.zone.id
            )
        } else {
            null
        }
    }
}
