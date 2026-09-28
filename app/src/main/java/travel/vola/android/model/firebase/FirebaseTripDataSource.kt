package travel.vola.android.model.firebase

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
class FirebaseTripDataSource(private val firestore: FirebaseFirestore) : TripDataSource {

    override val trips: Flow<List<Trip>> =
        firestore.collection("/trips").asFlow(this::tripConverter)

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
