package com.combah.travel2.model.repository.firebase

import com.combah.travel2.extensions.asObservable
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable

class FirebaseTripRepository(private val firestore: FirebaseFirestore) : TripRepository {
    override val trips: Observable<List<Trip>> = firestore.collection("/trips")
        .asObservable(this::tripConverter)

    override fun findTripById(tripId: String): Observable<Trip> {
        return firestore.document("/trips/$tripId")
            .asObservable(this::tripConverter)
    }

    private fun tripConverter(snapshot: DocumentSnapshot) = snapshot.toObject(Trip::class.java)?.copy(id = snapshot.id)
        ?: Trip(snapshot.id)
}