package com.combah.travel2.model.repository.firebase

import com.combah.travel2.extensions.asObservable
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable

class FirebaseTripRepository(firestore: FirebaseFirestore) : TripRepository {
    override val trips: Observable<List<Trip>> = firestore.collection("/trips")
        .asObservable()

    override fun findTripById(tripId: String): Observable<Trip> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}