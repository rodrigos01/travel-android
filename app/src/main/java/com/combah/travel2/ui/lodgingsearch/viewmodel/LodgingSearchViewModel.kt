package com.combah.travel2.ui.lodgingsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.remove
import com.combah.travel2.extensions.set
import com.combah.travel2.model.PlaceRepository
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.LodgingSearchRepository
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.lodgingsearch.state.LodgingDetailsState
import com.combah.travel2.ui.lodgingsearch.state.LodgingRoomOfferState
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone

class LodgingSearchViewModel(
    private val tripId: String,
    private val repository: LodgingSearchRepository,
    private val tripRepository: TripRepository,
    placeRepository: PlaceRepository,
    locationId: String,
    checkInMillis: Long,
    checkOutMillis: Long,
    timeZoneId: String,
) : ViewModel() {
    interface UiState {
        val searchState: SearchParamsState
        val localState: LocalState

        data class Loading(
            override val searchState: SearchParamsState,
            override val localState: LocalState = LocalState(),
        ) : UiState

        data class Loaded(
            override val searchState: SearchParamsState,
            val results: List<LodgingSearchResultState>,
            val openedResults: Map<String, LodgingDetailsState> = emptyMap(),
            override val localState: LocalState = LocalState(),
        ) : UiState
    }

    data class LocalState(
        val showAddConfirmation: Boolean = false,
    )

    data class SearchParamsState(
        val checkIn: Time,
        val checkOut: Time,
        val locationText: String,
        val minCheckOut: Time? = null,
    )

    private val location =
        placeRepository.places[locationId] ?: error("Place with id $locationId not found")

    private val loadingState = MutableStateFlow(false)
    private val searchParamsState = MutableStateFlow(
        SearchParamsState(
            checkIn = Time(checkInMillis, TimeZone.getTimeZone(timeZoneId)),
            checkOut = Time(checkOutMillis, TimeZone.getTimeZone(timeZoneId)),
            locationText = location.name,
        )
    )
    private val searchResultState = searchParamsState
        .onEach { loadingState.value = true }
        .map {
            it to repository.search(
                locationId = location.id,
                checkIn = it.checkIn,
                checkOut = it.checkOut,
            )
        }
        .onEach {
            loadingState.value = false
        }
    private val localState = MutableStateFlow(LocalState())
    private val openedResultsState = MutableMapStateFlow<String, LodgingDetailsState>()
    val uiState =
        combine(
            searchResultState,
            openedResultsState,
            loadingState,
            localState,
        ) { (params, results), openedResults, loading, localState ->
            if (loading) {
                UiState.Loading(searchState = params, localState = localState)
            } else {
                UiState.Loaded(
                    searchState = params,
                    localState = localState,
                    results = results.sortedBy { -it.reviewCount }.map {
                        LodgingSearchResultState(
                            id = it.id,
                            name = it.name,
                            coverImage = it.coverImage,
                            address = it.address,
                            rating = it.rating,
                            lodgingType = "${it.stars}-star hotel",
                            price = it.price,
                        )
                    },
                    openedResults = openedResults,
                )
            }
        }.stateIn(
            viewModelScope, SharingStarted.Eagerly, UiState.Loading(
                searchState = searchParamsState.value, localState = localState.value
            )
        )

    fun onLodgingTapped(lodgingId: String) {
        val state = uiState.value as? UiState.Loaded ?: return
        val existingState = state.results.firstOrNull { it.id == lodgingId } ?: return
        val initialState = LodgingDetailsState(
            name = existingState.name,
            rating = existingState.rating,
            reviewCountText = "",
            lodgingType = existingState.lodgingType,
            photos = listOf(existingState.coverImage),
            checkIn = state.searchState.checkIn,
            checkOut = state.searchState.checkOut,
            price = existingState.price,
            rooms = emptyList(),
            address = existingState.address,
            latitude = 0.0,
            longitude = 0.0,
            isLoading = true,
        )
        openedResultsState[lodgingId] = initialState
        viewModelScope.launch {
            val lodging =
                repository.details(
                    lodgingId,
                    searchParamsState.value.checkIn,
                    searchParamsState.value.checkOut
                )
                    ?: return@launch
            openedResultsState[lodgingId] = initialState.copy(
                photos = initialState.photos + lodging.photos.subList(1, lodging.photos.size),
                reviewCountText = lodging.reviewCount.toString(),
                latitude = lodging.latitude,
                longitude = lodging.longitude,
                rooms = lodging.rooms.map { offer ->
                    LodgingRoomOfferState(
                        photos = offer.photos,
                        description = offer.name,
                        breakfastIncluded = offer.features.breakfast,
                        refundable = offer.features.refundable,
                        prePaymentRequired = offer.features.prePayment,
                        isAllInclusive = offer.features.allInclusive,
                        price = offer.price,
                        bookingUrl = offer.bookingUrl,
                        bookingAgency = offer.bookingAgency,
                    )
                },
                isLoading = false,
            )
        }
    }

    fun onLodgingClosed(lodgingId: String) {
        openedResultsState.remove(lodgingId)
    }

    fun onAddLodgingTapped(lodgingId: String) {
        val details = openedResultsState[lodgingId] ?: return
        val lodging = Lodging(
            id = lodgingId,
            name = details.name,
            address = details.address,
            city = location,
            checkIn = details.checkIn,
            checkout = details.checkOut,
        )
        viewModelScope.launch {
            tripRepository.saveLodging(tripId, lodging)
            localState.value = localState.value.copy(showAddConfirmation = true)
        }
    }

    fun onContinueBrowsingTapped() {
        localState.value = localState.value.copy(showAddConfirmation = false)
    }
}