package com.combah.travel2.model.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FirebaseTripRepositoryTest {

    private val firestore = mock<FirebaseFirestore>()

    @Before
    fun setup() {
        whenever(firestore.collection(any()))
            .thenReturn(mock())
        whenever(firestore.document(any()))
            .thenReturn(mock())
    }

    @Test
    fun shouldGetTripsFromFirestore() {
        val repository = FirebaseTripRepository(firestore)

        verify(firestore).collection("/trips")
    }

    @Test
    fun shouldGetTripFromFirestore() {
        val repository = FirebaseTripRepository(firestore)

        repository.findTripById("myTrip").subscribe()
        verify(firestore).document("/trips/myTrip")
    }
}