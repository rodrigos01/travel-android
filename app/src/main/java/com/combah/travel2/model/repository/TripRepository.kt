package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    val trips: Flow<List<Trip>>

    fun findTripById(tripId: String): Flow<Trip>
    fun getTripFlights(tripId: String): Flow<List<Flight>>
    fun getTripHotels(tripId: String): Flow<List<Lodging>>

    suspend fun addTrip(): String
    suspend fun updateName(tripId: String, newName: String)
    suspend fun deleteTrip(tripId: String)
    suspend fun saveFlight(tripId: String, flight: Flight)
    suspend fun saveLodging(tripId: String, lodging: Lodging)
    suspend fun deleteFlight(tripId: String, flightId: String)
    suspend fun deleteLodging(tripId: String, lodgingId: String)
}
