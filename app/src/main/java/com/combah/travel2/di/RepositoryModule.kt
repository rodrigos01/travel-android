package com.combah.travel2.di

import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.model.repository.firebase.FirebaseTripRepository
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val repositoryModule = module {
    factory {
        FirebaseTripRepository(
                FirebaseFirestore.getInstance()
        ) as TripRepository
    }
}