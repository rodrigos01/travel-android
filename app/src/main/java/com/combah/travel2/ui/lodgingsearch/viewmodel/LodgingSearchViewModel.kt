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
    data class UiState(
        val checkIn: Time,
        val checkOut: Time,
        val locationText: String,
        val results: List<LodgingSearchResultState>,
        val openedResults: Map<String, LodgingDetailsState> = emptyMap(),
        val minCheckOut: Time? = null,
    )

    private data class SearchParams(
        val checkIn: Time,
        val checkOut: Time,
        val city: Place,
    )

    private val searchState = MutableStateFlow(
        SearchParams(
            checkIn = Time(checkInMillis, TimeZone.getTimeZone(timeZoneId)),
            checkOut = Time(checkOutMillis, TimeZone.getTimeZone(timeZoneId)),
            city = Place(locationId, locationName, "", 0.0, 0.0, null, "", ""),
        )
    )
    private val searchResultState = searchState.map {
        it to repository.search(
            locationId = it.city.id,
            checkIn = it.checkIn,
            checkOut = it.checkOut,
        )
    }
    private val openedResultsState = MutableMapStateFlow<String, LodgingDetailsState>()
    val uiState =
        combine(searchResultState, openedResultsState) { (params, results), openedResults ->
            UiState(
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
        }.map {
            it
        }.stateIn(
            viewModelScope, SharingStarted.Eagerly, UiState(
                searchState.value.checkIn,
                searchState.value.checkOut,
                searchState.value.city.name,
                emptyList()
            )
        )

    fun onLodgingTapped(lodgingId: String) {
        val existingState = uiState.value.results.first { it.id == lodgingId } ?: return
        openedResultsState[lodgingId] = LodgingDetailsState(
            name = existingState.name,
            rating = existingState.rating,
            reviewCountText = "",
            lodgingType = existingState.lodgingType,
            photos = listOf(existingState.coverImage),
            checkIn = uiState.value.checkIn,
            checkOut = uiState.value.checkOut,
            price = existingState.price,
            rooms = emptyList(),
            address = existingState.address,
            latitude = 0.0,
            longitude = 0.0,
        )
        viewModelScope.launch {
            val lodging =
                repository.details(lodgingId, searchState.value.checkIn, searchState.value.checkOut)
                    ?: return@launch
            openedResultsState[lodgingId] = LodgingDetailsState(name = lodging.name,
                rating = lodging.rating,
                price = lodging.price,
                address = lodging.address,
                photos = lodging.photos,
                reviewCountText = lodging.reviewCount.toString(),
                checkIn = searchState.value.checkIn,
                checkOut = searchState.value.checkOut,
                lodgingType = "${lodging.stars}-star hotel",
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
                })
        }
    }

    fun onLodgingClosed(lodgingId: String) {
        openedResultsState.remove(lodgingId)
    }
}