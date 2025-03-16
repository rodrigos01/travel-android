package com.combah.travel2.ui.lodgingsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.TimeZone
import kotlin.time.Duration.Companion.days

class LodgingSearchViewModel(
    repository: AddLodgingRepository,
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
    val uiState = searchState.map {
        it to repository.search(
            locationId = it.city.id,
            checkIn = it.checkIn,
            checkOut = it.checkOut,
        )
    }.map { (params, results) ->
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
            }
        )
    }.map {
        it
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        UiState(
            searchState.value.checkIn,
            searchState.value.checkOut,
            searchState.value.city.name,
            emptyList()
        )
    )
}