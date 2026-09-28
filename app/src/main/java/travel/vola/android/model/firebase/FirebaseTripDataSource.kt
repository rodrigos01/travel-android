package travel.vola.android.model.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import travel.vola.android.extensions.asFlow
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripDataSource

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseTripDataSource(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TripDataSource {

    // A getter, not a val: security rules will restrict /trips reads to the
    // caller's own documents, so this must read currentUser fresh on each
    // subscription rather than bake in whoever was signed in when this data
    // source was constructed.
    override val trips: Flow<List<Trip>>
        get() = firestore.collection("/trips")
            .whereEqualTo("ownerId", firebaseAuth.currentUser?.uid)
            .asFlow(this::tripConverter)

    override fun findTripById(tripId: String): Flow<Trip?> {
        return firestore.document("/trips/$tripId").asFlow(this::tripConverter)
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId).map { it?.flights ?: emptyList() }
    }

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> {
        return findTripById(tripId).map { it?.lodgings ?: emptyList() }
    }

    private fun tripConverter(snapshot: DocumentSnapshot) =
        (
            snapshot.toObject(FirebaseData.Trip::class.java)?.copy(id = snapshot.id)
                ?: FirebaseData.Trip(snapshot.id)
            ).toAppDataModel()
}
