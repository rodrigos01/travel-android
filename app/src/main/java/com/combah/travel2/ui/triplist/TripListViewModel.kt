package com.combah.travel2.ui.triplist

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.model.repository.TripRepository

class TripListViewModel(repository: TripRepository) : ViewModel() {
    val trips = repository.trips.asLiveData()
}