package com.combah.travel2.ui.lodgingsearch.viewmodel

import androidx.lifecycle.ViewModel
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LodgingSearchViewModel(
    repository: AddLodgingRepository,
    checkIn: Time,
    checkOut: Time,
    location: Place,
) : ViewModel() {
    data class UiState(
        val checkIn: Time,
        val checkOut: Time,
        val locationText: String,
        val results: List<LodgingSearchResultState>,
    )

    private data class SearchParams(
        val checkIn: Time,
        val checkOut: Time,
        val city: Place,
    )

    private val searchState = MutableStateFlow(
        SearchParams(
            checkIn = checkIn,
            checkOut = checkOut,
            city = location,
        )
    )
    val uiState = searchState.map {
        it to repository.search(
            locationId = it.city.id,
            checkIn = it.checkIn,
            checkOut = it.checkOut
        )
    }.map { (params, results) ->
        UiState(
            checkIn = params.checkIn,
            checkOut = params.checkOut,
            locationText = params.city.name,
            results = results.map {
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
    }
}