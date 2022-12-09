package com.combah.travel2.model.repository.firebase

import com.combah.travel2.extensions.asFlow
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseTripRepository(private val firestore: FirebaseFirestore) : TripRepository {
    override val trips: Flow<List<Trip>> = firestore.collection("/trips")
        .asFlow(this::tripConverter)

    override fun findTripById(tripId: String): Flow<Trip> {
        return firestore.document("/trips/$tripId")
            .asFlow(this::tripConverter)
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId)
            .map { it.flights ?: emptyList() }
    }

    override fun getTripHotels(tripId: String): Flow<List<Hotel>> {
        return findTripById(tripId)
            .map { it.hotels ?: emptyList() }
    }

    private fun tripConverter(snapshot: DocumentSnapshot) =
        snapshot.toObject(Trip::class.java)?.copy(id = snapshot.id)
            ?: Trip(snapshot.id)
}