package travel.vola.android.model.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import travel.vola.android.extensions.expectItem
import travel.vola.android.model.repository.mock.MockData

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

    private val mockQuery = mock<Query> {
        on { addSnapshotListener(any()) } doAnswer {
            it.getArgument<EventListener<QuerySnapshot>>(0).onEvent(mockQuerySnapshot, null)
            ListenerRegistration { }
        }
    }
    private val mockCollectionReference = mock<CollectionReference> {
        on { whereEqualTo(any<String>(), any()) } doReturn mockQuery
    }
    private val mockQuerySnapshot = mock<QuerySnapshot> {
        on { documents } doReturn listOf(mockDocumentSnapshot)
    }
    private val firestore = mock<FirebaseFirestore> {
        on { collection(any()) } doReturn mockCollectionReference
        on { document(any()) } doReturn mockDocumentReference
    }
    private val mockUser = mock<FirebaseUser> {
        on { uid } doReturn "owner-uid"
    }
    private val firebaseAuth = mock<FirebaseAuth> {
        on { currentUser } doReturn mockUser
    }

    @Test
    fun shouldGetTripsFromFirestore() = runTest {
        val repository = FirebaseTripDataSource(firestore, firebaseAuth)

        Assert.assertEquals(listOf(MockData.trip.toAppDataModel()), repository.trips.expectItem())
        verify(firestore).collection("/trips")
        verify(mockCollectionReference).whereEqualTo("ownerId", "owner-uid")
    }

    @Test
    fun shouldGetTripFromFirestore() = runTest {
        val repository = FirebaseTripDataSource(firestore, firebaseAuth)

        val tripObservable = repository.findTripById("myTrip")

        Assert.assertEquals(MockData.trip.toAppDataModel(), tripObservable.expectItem())
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripFlights() = runTest {
        val repository = FirebaseTripDataSource(firestore, firebaseAuth)

        val flightsObservable = repository.getTripFlights("myTrip")

        Assert.assertEquals(
            MockData.trip.flights.map { it.toAppDataModel() },
            flightsObservable.expectItem(),
        )
        verify(firestore).document("/trips/myTrip")
    }

    @Test
    fun shouldGetTripHotels() = runTest {
        val repository = FirebaseTripDataSource(firestore, firebaseAuth)

        val hotelsObservable = repository.getTripHotels("myTrip")

        Assert.assertEquals(
            MockData.trip.lodgings.map { it.toAppDataModel() },
            hotelsObservable.expectItem(),
        )
        verify(firestore).document("/trips/myTrip")
    }
}
