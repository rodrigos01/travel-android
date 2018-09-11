package com.combah.travel2.ui

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.model.repository.TripRepository
import javax.inject.Inject

class TripListViewModel @Inject constructor(repository: TripRepository) : ViewModel() {
    val trips = repository.trips.asLiveData()
}