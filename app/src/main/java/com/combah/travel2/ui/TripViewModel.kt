package com.combah.travel2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.Event
import javax.inject.Inject

class TripViewModel constructor(repository: TripRepository, tripId: String) : ViewModel() {

    private val trip = repository.findTripById(tripId)

    val events = trip.filter { it.flights != null }
        .map(this::getEventsFromTrip)
        .asLiveData()

    private fun getEventsFromTrip(trip: Trip) = trip.flights
        ?.flatMap { it.segments }
        ?.flatMap {
            listOf(
                Event(
                    it.cityTo.name,
                    it.airportFrom.name,
                    it.departure
                ),
                Event(
                    "Arrival",
                    it.airportTo.name,
                    it.arrival
                )
            )
        }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repository: TripRepository) : ViewModelProvider.Factory {

        private lateinit var tripId: String

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TripViewModel(repository, tripId) as T
        }

    }
}