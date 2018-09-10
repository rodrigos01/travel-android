package com.combah.travel2.di

import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.model.repository.firebase.FirebaseTripRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class RepositoryModule {

    @Provides
    fun providesTripRepository(): TripRepository {
        return FirebaseTripRepository(
            FirebaseFirestore.getInstance()
        )
    }

}