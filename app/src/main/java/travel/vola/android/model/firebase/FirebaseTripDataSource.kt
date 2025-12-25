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
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripPreferences
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
        val newTrip = FirebaseData.Trip()
        val reference = firestore.collection("/trips").add(newTrip).await()
        return reference.id
    }

    override suspend fun addTrip(
        name: String,
        places: List<TimedPlace>,
        preferences: TripPreferences
    ): String {
        val newTrip = FirebaseData.Trip(
            name = name,
            places = places.map { it.toFirebaseDataModel() },
            preferences = preferences.toFirebaseDataModel(),
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
        firestore.document("/trips/$tripId").update(
            "flights",
            trip.flights.addOrReplace(flight.toFirebaseDataModel()) { it.id == flight.id },
        )
    }

    override suspend fun deleteFlight(tripId: String, flightId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "flights",
            trip.flights.filterNot { it.id == flightId },
        )
    }

    override suspend fun saveLodging(tripId: String, lodging: Lodging) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "lodgings",
            trip.lodgings.addOrReplace(lodging.toFirebaseDataModel()) { it.id == lodging.id },
        )
    }

    override suspend fun deleteLodging(tripId: String, lodgingId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId")
            .update(
                "lodgings",
                trip.lodgings.filterNot { it.id == lodgingId },
            )
    }

    override suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "places",
            trip.places.addOrReplace(timedPlace.toFirebaseDataModel()) { it.id == timedPlace.id },
        )
    }

    override suspend fun saveFlexibleSection(
        tripId: String,
        flexibleSection: FlexibleDaySection
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "places",
            trip.places.filterNot { it.id == timedPlaceId },
        )
    }

    override suspend fun saveRestaurantReservation(
        tripId: String, restaurantReservation: RestaurantReservation
    ) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "restaurants",
            trip.restaurants.addOrReplace(restaurantReservation.toFirebaseDataModel()) { it.id == restaurantReservation.id },
        )
    }

    override suspend fun deleteRestaurantReservation(
        tripId: String, restaurantReservationId: String
    ) {
        val trip = getTrip(tripId).toObject<FirebaseData.Trip>() ?: return
        firestore.document("/trips/$tripId").update(
            "restaurants",
            trip.restaurants.filterNot { it.id == restaurantReservationId },
        )
    }

    override suspend fun deleteFlexibleSection(
        tripId: String,
        flexibleSectionId: String
    ) {
        TODO("Not yet implemented")
    }

    private suspend fun getTrip(tripId: String) = firestore.document("/trips/$tripId").get().await()
}

private fun <T> List<T>.addOrReplace(item: T, predicate: (T) -> Boolean): List<T> =
    toMutableList().apply {
        val index = indexOfFirst(predicate)
        if (index != -1) {
            // update previously saved item
            removeAt(index)
            add(index, item)
        } else {
            add(item)
        }
    }.toList()
