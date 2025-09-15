package travel.vola.android.model.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import travel.vola.android.extensions.asFlow
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripDataSource

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseTripDataSource(private val firestore: FirebaseFirestore) : TripDataSource {

    override val dataSourceType: DataSourceType = DataSourceType.FIREBASE

    override val trips: Flow<List<Trip>> =
        firestore.collection("/trips").asFlow(this::tripConverter)

    override fun findTripById(tripId: String): Flow<Trip> {
        return firestore.document("/trips/$tripId").asFlow(this::tripConverter)
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId).map { it.flights }
    }

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> {
        return findTripById(tripId).map { it.lodgings }
    }

    private fun tripConverter(snapshot: DocumentSnapshot) =
        (snapshot.toObject(FirebaseData.Trip::class.java)?.copy(id = snapshot.id)
            ?: FirebaseData.Trip(snapshot.id)).toAppDataModel()

    override suspend fun addTrip(): String {
        val newTrip = Trip(
            id = "",
            name = null,
            coverImage = null,
            flights = emptyList(),
            lodgings = emptyList(),
            places = emptyList(),
            restaurants = emptyList(),
        )
        val reference = firestore.collection("/trips").add(newTrip).await()
        return reference.id
    }

    override suspend fun updateName(tripId: String, newName: String) {
        firestore.document("/trips/$tripId").update("name", newName)
    }

    override suspend fun deleteTrip(tripId: String) {
        firestore.document("/trips/$tripId").delete()
    }

    override suspend fun saveFlight(tripId: String, flight: Flight) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update("flights", trip.flights.toMutableList().apply {
            val firebaseFlight = flight.toFirebaseDataModel()
            // removes the previously saved lodging with same id
            val index = indexOfFirst { it.id == flight.id }
            if (index != -1) {
                // update previously saved item
                removeAt(index)
                add(index, firebaseFlight)
            } else {
                add(firebaseFlight)
            }
        }.toList())
    }

    override suspend fun deleteFlight(tripId: String, flightId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update("flights", trip.flights.toMutableList().apply {
            removeIf { it.id == flightId }
        }.toList())
    }

    override suspend fun saveLodging(tripId: String, lodging: Lodging) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update("lodgings", trip.lodgings.toMutableList().apply {
                val firebaseLodging = lodging.toFirebaseDataModel()
                // removes the previously saved lodging with same id
                val index = indexOfFirst { it.id == lodging.id }
                if (index != -1) {
                    // update previously saved item
                    removeAt(index)
                    add(index, firebaseLodging)
                } else {
                    add(firebaseLodging)
                }
            }.toList())
    }

    override suspend fun deleteLodging(tripId: String, lodgingId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update("lodgings", trip.lodgings.toMutableList().apply {
                removeIf { it.id == lodgingId }
            }.toList())
    }

    override suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update("places", trip.places.toMutableList().apply {
                val firebasePlace = timedPlace.toFirebaseDataModel()
                val index = indexOfFirst { it.id == firebasePlace.id }
                if (index != -1) {
                    // update previously saved item
                    removeAt(index)
                    add(index, firebasePlace)
                } else {
                    add(firebasePlace)
                }
            }.toList())
    }

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update("places", trip.places.toMutableList().apply {
                removeIf { it.id == timedPlaceId }
            }.toList())
    }

    private suspend fun getTrip(tripId: String) = firestore.document("/trips/$tripId").get().await()
}
