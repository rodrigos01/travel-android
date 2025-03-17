package com.combah.travel2.ui.lodgingsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.remove
import com.combah.travel2.extensions.set
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
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
import kotlin.time.Duration.Companion.days

class LodgingSearchViewModel(
    private val repository: AddLodgingRepository,
    checkInMillis: Long,
    checkOutMillis: Long,
    locationId: String,
    locationName: String,
    timeZoneId: String,
) : ViewModel() {
    interface UiState {
        val checkIn: Time
        val checkOut: Time
        val locationText: String
        val minCheckOut: Time?

        data class Loading(
            override val checkIn: Time,
            override val checkOut: Time,
            override val locationText: String,
            override val minCheckOut: Time? = null,
        ) : UiState

        data class Loaded(
            override val checkIn: Time,
            override val checkOut: Time,
            override val locationText: String,
            val results: List<LodgingSearchResultState>,
            val openedResults: Map<String, LodgingDetailsState> = emptyMap(),
            override val minCheckOut: Time? = null,
        ) : UiState
    }

    private data class SearchParams(
        val checkIn: Time,
        val checkOut: Time,
        val city: Place,
    )

    private val loadingState = MutableStateFlow(false)
    private val searchState = MutableStateFlow(
        SearchParams(
            checkIn = Time(checkInMillis, TimeZone.getTimeZone(timeZoneId)),
            checkOut = Time(checkOutMillis, TimeZone.getTimeZone(timeZoneId)),
            city = Place(locationId, locationName, "", 0.0, 0.0, null, "", ""),
        )
    )
    private val searchResultState = searchState
        .onEach { loadingState.value = true }
        .map {
            it to repository.search(
                locationId = it.city.id,
                checkIn = it.checkIn,
                checkOut = it.checkOut,
            )
        }
        .onEach {
            loadingState.value = false
        }
    private val openedResultsState = MutableMapStateFlow<String, LodgingDetailsState>()
    val uiState =
        combine(
            searchResultState,
            openedResultsState,
            loadingState
        ) { (params, results), openedResults, loading ->
            if (loading) {
                UiState.Loading(
                    checkIn = params.checkIn,
                    checkOut = params.checkOut,
                    locationText = params.city.name,
                )
            } else {
                UiState.Loaded(
                    checkIn = params.checkIn,
                    checkOut = params.checkOut,
                    minCheckOut = params.checkIn.toMidnight() + 1.days,
                    locationText = params.city.name,
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
                searchState.value.checkIn,
                searchState.value.checkOut,
                searchState.value.city.name,
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
            checkIn = state.checkIn,
            checkOut = state.checkOut,
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
                repository.details(lodgingId, searchState.value.checkIn, searchState.value.checkOut)
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
}