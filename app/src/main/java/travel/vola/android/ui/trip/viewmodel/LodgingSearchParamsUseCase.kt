package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.plus
import travel.vola.android.extensions.timeInMillis
import travel.vola.android.extensions.toMidnight
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.creation.usecase.currentAs
import travel.vola.android.ui.trip.creation.usecase.requireCurrent
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.SearchResultItemState
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days

interface LodgingSearchParamsFactory {
    fun getLodgingSearchParams(tripId: String, item: LodgingSearchItemState): LodgingSearchDestination.Params?
}

class LodgingSearchParamsUseCase(
    private val pendingDataStore: PendingDataStore,
    private val repository: LodgingSearchRepository = LodgingSearchRepository(),
    private val placeRepository: PlaceRepository,
) : AddPlanUseCase.AddItemUseCase<Lodging, LodgingSearchItemState>,
    LodgingSearchParamsFactory {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): LodgingSearchItemState = createItem(id, checkIn = time, checkOut = null, city = null, params)

    fun createItem(
        id: String,
        checkIn: ZonedDateTime,
        checkOut: ZonedDateTime?,
        city: Place?,
        params: AddPlanUseCase.StateParams,
    ): LodgingSearchItemState {
        pendingDataStore.set(PendingDataStore.Entry.LodgingSearch(city = city))
        return LodgingSearchItemState(
            id = id,
            timestamp = checkIn,
            saveButtonEnabled = city != null && checkOut != null && checkOut > checkIn,
            dateSelectionEnabled = params.dateSelectionEnabled,
            deleteButtonEnabled = params.deleteEnabled,
            typeSelectionEnabled = params.typeSelectionEnabled,
            checkIn = checkIn,
            minCheckOutTime = checkIn.toMidnight() + 1.days,
            checkOut = checkOut,
            locationText = city?.name,
            searchResults = emptyList(),
        )
    }

    override fun createItem(
        id: String,
        entity: Lodging,
        params: AddPlanUseCase.StateParams,
    ): LodgingSearchItemState = createItem(
        id = id,
        checkIn = entity.checkIn,
        checkOut = entity.checkout,
        city = null, // TODO: Use city from entity when Unified Places API is available
        params = params,
    )

    override suspend fun onUpdated(state: LodgingSearchItemState): LodgingSearchItemState {
        val base = state.copy(minCheckOutTime = state.checkIn.toMidnight() + 1.days)
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.LodgingSearch>()
        val selectedId = state.selectedResultId
        val text = state.locationText
        val selectedCity = if (selectedId != null && selectedId != entry.city?.id) {
            entry.searchResults.firstOrNull { it.id == selectedId }
        } else {
            null
        }
        val resolved = if (selectedCity != null) {
            pendingDataStore.update { (it as PendingDataStore.Entry.LodgingSearch).copy(city = selectedCity) }
            base.copy(locationText = selectedCity.name, searchResults = emptyList())
        } else if (selectedId == null && text != null && text.length >= 3) {
            val results = repository.autocompleteCity(text)
            pendingDataStore.update { (it as PendingDataStore.Entry.LodgingSearch).copy(searchResults = results) }
            base.copy(searchResults = results.map { SearchResultItemState(it.id, it.name, it.address) })
        } else {
            base
        }
        val updatedEntry = pendingDataStore.requireCurrent<PendingDataStore.Entry.LodgingSearch>()
        return resolved.copy(
            saveButtonEnabled = updatedEntry.city != null && resolved.checkOut != null && resolved.checkOut > resolved.checkIn,
        )
    }

    override fun getLodgingSearchParams(
        tripId: String,
        item: LodgingSearchItemState,
    ): LodgingSearchDestination.Params? {
        val entry = pendingDataStore.currentAs<PendingDataStore.Entry.LodgingSearch>() ?: return null
        val city = entry.city ?: return null
        val checkOut = item.checkOut ?: return null
        placeRepository.places[city.id] = city
        return LodgingSearchDestination.Params(
            tripId = tripId,
            checkIn = item.checkIn.timeInMillis,
            checkOut = checkOut.timeInMillis,
            locationId = city.id,
            timeZoneId = item.checkIn.zone.id,
        )
    }
}
