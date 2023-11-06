package com.combah.travel2.ui.triplist

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.map

class TripListViewModel(repository: TripRepository) : ViewModel() {
    data class ViewState(
        val trips: List<Trip>
    )

    val viewState = repository.trips.map {
        ViewState(trips = it)
    }.asStateFlow(initialValue = ViewState(emptyList()))
}