package com.combah.travel2.model.firebase

import com.combah.travel2.extensions.expectItem
import com.combah.travel2.model.repository.mock.MockData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class FirebaseTripRepositoryTest {

    private val mockDocumentReference = mock<DocumentReference> {
        on { addSnapshotListener(any()) } doAnswer {
            it.getArgument<EventListener<DocumentSnapshot>>(0).onEvent(mockDocumentSnapshot, null)
            ListenerRegistration {}
        }
    }
    private val mockDocumentSnapshot = mock<DocumentSnapshot> {
        on { toObject(FirebaseData.Trip::class.java) } doReturn MockData.trip
        on { id } doReturn MockData.trip.id
    }

    private val mockCollectionReference = mock<CollectionReference> {
        on { addSnapshotListener(any()) } doAnswer {
            it.getArgument<EventListener<QuerySnapshot>>(0).onEvent(mockQuerySnapshot, null)
            ListenerRegistration { }
        }
    }
    private val mockQuerySnapshot = mock<QuerySnapshot> {
        on { documents } doReturn listOf(mockDocumentSnapshot)
    }
    private val firestore = mock<FirebaseFirestore> {
        on { collection(any()) } doReturn mockCollectionReference
        on { document(any()) } doReturn mockDocumentReference
    }

    @Test
    fun shouldGetTripsFromFirestore() = runTest {
        val repository = FirebaseTripRepository(firestore)

        Assert.assertEquals(listOf(MockData.trip.toAppDataModel()), repository.trips.expectItem())
        verify(firestore).collection("/trips")
    }

    @Test
    fun shouldGetTripFromFirestore() = runTest {
        val repository = FirebaseTripRepository(firestore)

        val tripObservable = repository.findTripById("myTrip")

        Assert.assertEquals(MockData.trip.toAppDataModel(), tripObservable.expectItem())
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripFlights() = runTest {
        val repository = FirebaseTripRepository(firestore)

        val flightsObservable = repository.getTripFlights("myTrip")

        Assert.assertEquals(
            MockData.trip.flights.map { it.toAppDataModel() },
            flightsObservable.expectItem()
        )
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripHotels() = runTest {
        val repository = FirebaseTripRepository(firestore)

        val hotelsObservable = repository.getTripHotels("myTrip")

        Assert.assertEquals(
            MockData.trip.lodgings.map { it.toAppDataModel() },
            hotelsObservable.expectItem()
        )
        verify(firestore).document("/trips/myTrip")
    }
}