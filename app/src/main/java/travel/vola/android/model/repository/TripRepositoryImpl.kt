package travel.vola.android.model.repository

import kotlinx.coroutines.flow.Flow
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripPreferences

// CQRS-lite: reads go straight to Firestore (queries), writes go through
// the travel-node command API (commands) so the server can validate and
// own the write path instead of trusting the client SDK.
class TripRepositoryImpl(
    private val queries: TripDataSource,
    private val commands: TripCommandDataSource,
) : TripRepository {

    override val trips: Flow<List<Trip>> = queries.trips

    override fun findTripById(tripId: String): Flow<Trip?> = queries.findTripById(tripId)

    override fun getTripFlights(tripId: String): Flow<List<Flight>> =
        queries.getTripFlights(tripId)

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> =
        queries.getTripHotels(tripId)

    override suspend fun addTrip(): String = commands.addTrip()

    override suspend fun addTrip(
        name: String,
        places: List<TimedPlace>,
        preferences: TripPreferences,
    ): String = commands.addTrip(name, places, preferences)

    override suspend fun updateName(tripId: String, newName: String) =
        commands.updateName(tripId, newName)

    override suspend fun updateTripPreferences(
        tripId: String,
        preferences: TripPreferences,
    ) = commands.updateTripPreferences(tripId, preferences)

    override suspend fun deleteTrip(tripId: String) = commands.deleteTrip(tripId)

    override suspend fun saveFlight(tripId: String, flight: Flight) =
        commands.saveFlight(tripId, flight)

    override suspend fun saveLodging(tripId: String, lodging: Lodging) =
        commands.saveLodging(tripId, lodging)

    override suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace) =
        commands.saveTimedPlace(tripId, timedPlace)

    override suspend fun saveRestaurantReservation(
        tripId: String,
        restaurantReservation: RestaurantReservation,
    ) = commands.saveRestaurantReservation(tripId, restaurantReservation)

    override suspend fun saveFlexibleSection(
        tripId: String,
        flexibleSection: FlexibleDaySection,
    ) = commands.saveFlexibleSection(tripId, flexibleSection)

    override suspend fun deleteFlight(tripId: String, flightId: String) =
        commands.deleteFlight(tripId, flightId)

    override suspend fun deleteLodging(tripId: String, lodgingId: String) =
        commands.deleteLodging(tripId, lodgingId)

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) =
        commands.deleteTimedPlace(tripId, timedPlaceId)

    override suspend fun deleteRestaurantReservation(
        tripId: String,
        restaurantReservationId: String,
    ) = commands.deleteRestaurantReservation(tripId, restaurantReservationId)

    override suspend fun deleteFlexibleSection(tripId: String, flexibleSectionId: String) =
        commands.deleteFlexibleSection(tripId, flexibleSectionId)
}
