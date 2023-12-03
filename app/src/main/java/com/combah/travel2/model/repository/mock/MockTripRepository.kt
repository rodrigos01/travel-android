package com.combah.travel2.model.repository.mock

import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockTripRepository : TripRepository {
    override val trips: Flow<List<Trip>>
        get() = flowOf(MockData.tripList)

    override fun findTripById(tripId: String): Flow<Trip> = flowOf(MockData.trip)

    override fun getTripFlights(tripId: String): Flow<List<Flight>> =
        flowOf(MockData.trip.flights ?: emptyList())

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> =
        flowOf(MockData.trip.lodgings ?: emptyList())
}