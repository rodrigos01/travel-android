package com.combah.travel2.model.firebase

import com.combah.travel2.extensions.asFlow
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseTripRepository(private val firestore: FirebaseFirestore) : TripRepository {
    override val trips: Flow<List<Trip>> =
        firestore.collection("/trips").asFlow(this::tripConverter)

    override fun findTripById(tripId: String): Flow<Trip> {
        return firestore.document("/trips/$tripId").asFlow(this::tripConverter)
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId).map { it.flights }
    }

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> {
        return findTripById(tripId).map { it.lodgings }
    }

    private fun tripConverter(snapshot: DocumentSnapshot) =
        (snapshot.toObject(FirebaseData.Trip::class.java)?.copy(id = snapshot.id)
            ?: FirebaseData.Trip(snapshot.id)).toAppDataModel()

    override suspend fun addTrip(): String {
        val newTrip = Trip(
            id = "",
            name = null,
            coverImage = null,
            flights = emptyList(),
            lodgings = emptyList(),
            places = emptyList(),
        )
        val reference = firestore.collection("/trips").add(newTrip).await()
        return reference.id
    }

    override suspend fun updateName(tripId: String, newName: String) {
        firestore.document("/trips/$tripId").update("name", newName)
    }

    override suspend fun addFlight(tripId: String, flight: Flight) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update("flights", trip.flights.toMutableList().apply {
            add(flight.toFirebaseDataModel())
        }.toList())
    }

    override suspend fun addLodging(tripId: String, lodging: Lodging) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update("lodgings", trip.lodgings.toMutableList().apply {
                add(lodging.toFirebaseDataModel())
            }.toList())
    }

    private suspend fun getTrip(tripId: String) = firestore.document("/trips/$tripId").get().await()
}
