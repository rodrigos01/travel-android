package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.data.CheckinEvent
import com.combah.travel2.ui.data.CheckoutEvent
import com.combah.travel2.ui.data.FlightEvent
import javax.inject.Inject

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    private val trip = repository.findTripById(tripId)

    val events = trip.filter { it.flights != null }
        .map(this::getEventsFromTrip)
        .asLiveData()

    private fun getEventsFromTrip(trip: Trip) = getFlightEventsFromTrip(trip)
        ?.asSequence()
        ?.plus(getHotelEventsFromTrip(trip) ?: emptyList())
        ?.sortedBy { it.timestamp }
        ?.toList()

    private fun getFlightEventsFromTrip(trip: Trip) = trip.flights
        ?.flatMap { it.segments.flatMap(this::getFlightEventsFromSegment) }

    private fun getFlightEventsFromSegment(segment: FlightSegment) = listOf(
        FlightEvent(
            segment.cityTo,
            segment.airportFrom,
            segment.departure
        ),
        ArrivalEvent(
            segment.airportTo,
            segment.arrival
        )
    )

    private fun getHotelEventsFromTrip(trip: Trip) = trip.hotels?.flatMap {
        listOf(
            CheckinEvent(
                it.name,
                it.checkin
            ),
            CheckoutEvent(
                it.name,
                it.checkout
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repository: TripRepository) : ViewModelProvider.Factory {

        lateinit var tripId: String

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TripViewModel(repository, tripId) as T
        }

    }
}