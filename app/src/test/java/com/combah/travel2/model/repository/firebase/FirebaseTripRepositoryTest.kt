package com.combah.travel2.model.repository.firebase

import com.combah.travel2.assertObservableEquals
import com.combah.travel2.mock.trip
import com.combah.travel2.model.data.Trip
import com.google.firebase.firestore.*
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
        val mockDocumentReference = mock<DocumentReference>()
        val mockDocumentSnapshot = mock<DocumentSnapshot>()

        val mockCollectionReference = mock<CollectionReference>()
        val mockQuerySnapshot = mock<QuerySnapshot>()

        whenever(firestore.collection(any()))
                .thenReturn(mockCollectionReference)
        whenever(firestore.document(any()))
                .thenReturn(mockDocumentReference)

        whenever(mockDocumentSnapshot.toObject(Trip::class.java))
                .thenReturn(trip)
        whenever(mockDocumentSnapshot.id).thenReturn(trip.id)

        whenever(mockDocumentReference.addSnapshotListener(any())).then {
            val listener = it.arguments[0] as EventListener<DocumentSnapshot>

            listener.onEvent(mockDocumentSnapshot, null)
            mock<ListenerRegistration>()
        }

        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

        whenever(mockCollectionReference.addSnapshotListener(any())).then {
            val listener = it.arguments[0] as EventListener<QuerySnapshot>

            listener.onEvent(mockQuerySnapshot, null)
            mock<ListenerRegistration>()
        }
    }

    @Test
    fun shouldGetTripsFromFirestore() {
        val repository = FirebaseTripRepository(firestore)

        assertObservableEquals(listOf(trip), repository.trips)
        verify(firestore).collection("/trips")
    }

    @Test
    fun shouldGetTripFromFirestore() {
        val repository = FirebaseTripRepository(firestore)

        val tripObservable = repository.findTripById("myTrip")

        assertObservableEquals(trip, tripObservable)
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripFlights() {
        val repository = FirebaseTripRepository(firestore)

        val flightsObservable = repository.getTripFlights("myTrip")

        assertObservableEquals(trip.flights, flightsObservable)
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripHotels() {
        val repository = FirebaseTripRepository(firestore)

        val hotelsObservable = repository.getTripHotels("myTrip")

        assertObservableEquals(trip.hotels, hotelsObservable)
        verify(firestore).document("/trips/myTrip")
    }
}