package com.combah.travel2.di

import com.combah.travel2.model.PlaceRepository
import com.combah.travel2.model.firebase.FirebaseTripRepository
import com.combah.travel2.model.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore

class ServiceLocator {
    val tripRepository: TripRepository by lazy {
        FirebaseTripRepository(FirebaseFirestore.getInstance())
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }
}