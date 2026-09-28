package travel.vola.android.model.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripPreferences
import travel.vola.android.model.firebase.FirebaseData
import travel.vola.android.model.firebase.toFirebaseDataModel
import travel.vola.android.model.repository.TripCommandDataSource
import java.util.UUID

class TripCommandException(message: String) : Exception(message)

// Fields left at their default (null) are omitted from the request body, so
// a PUT that only sets `name` leaves `preferences`/`places` untouched
// server-side — see TripsController#upsertTrip on the API side.
@Serializable
private data class TripUpdate(
    val name: String? = null,
    val preferences: FirebaseData.TripPreferences? = null,
    val places: List<FirebaseData.TimedPlace>? = null,
)

private val commandJson = Json { encodeDefaults = false }

class ApiTripCommandDataSource : TripCommandDataSource {

    override suspend fun addTrip(): String {
        val tripId = UUID.randomUUID().toString()
        putTrip(tripId, TripUpdate())
        return tripId
    }

    override suspend fun addTrip(
        name: String,
        places: List<TimedPlace>,
        preferences: TripPreferences,
    ): String {
        val tripId = UUID.randomUUID().toString()
        putTrip(
            tripId,
            TripUpdate(
                name = name,
                places = places.map { it.toFirebaseDataModel() },
                preferences = preferences.toFirebaseDataModel(),
            ),
        )
        return tripId
    }

    override suspend fun updateName(tripId: String, newName: String) {
        putTrip(tripId, TripUpdate(name = newName))
    }

    override suspend fun updateTripPreferences(tripId: String, preferences: TripPreferences) {
        putTrip(tripId, TripUpdate(preferences = preferences.toFirebaseDataModel()))
    }

    override suspend fun deleteTrip(tripId: String) {
        deleteCommand("trips/$tripId")
    }

    override suspend fun saveFlight(tripId: String, flight: Flight) {
        putCommand("trips/$tripId/flights/${flight.id}", flight.toFirebaseDataModel())
    }

    override suspend fun deleteFlight(tripId: String, flightId: String) {
        deleteCommand("trips/$tripId/flights/$flightId")
    }

    override suspend fun saveLodging(tripId: String, lodging: Lodging) {
        putCommand("trips/$tripId/lodgings/${lodging.id}", lodging.toFirebaseDataModel())
    }

    override suspend fun deleteLodging(tripId: String, lodgingId: String) {
        deleteCommand("trips/$tripId/lodgings/$lodgingId")
    }

    override suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace) {
        putCommand("trips/$tripId/places/${timedPlace.id}", timedPlace.toFirebaseDataModel())
    }

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) {
        deleteCommand("trips/$tripId/places/$timedPlaceId")
    }

    override suspend fun saveRestaurantReservation(
        tripId: String,
        restaurantReservation: RestaurantReservation,
    ) {
        putCommand(
            "trips/$tripId/restaurants/${restaurantReservation.id}",
            restaurantReservation.toFirebaseDataModel(),
        )
    }

    override suspend fun deleteRestaurantReservation(
        tripId: String,
        restaurantReservationId: String,
    ) {
        deleteCommand("trips/$tripId/restaurants/$restaurantReservationId")
    }

    override suspend fun saveFlexibleSection(tripId: String, flexibleSection: FlexibleDaySection) {
        putCommand(
            "trips/$tripId/flexible-sections/${flexibleSection.id}",
            flexibleSection.toFirebaseDataModel(),
        )
    }

    override suspend fun deleteFlexibleSection(tripId: String, flexibleSectionId: String) {
        deleteCommand("trips/$tripId/flexible-sections/$flexibleSectionId")
    }

    private suspend fun putTrip(tripId: String, body: TripUpdate) {
        val path = "trips/$tripId"
        put(path, commandJson.encodeToString(body)).ensureCommandSucceeded(path)
    }

    private suspend inline fun <reified T> putCommand(path: String, body: T) {
        put(path, commandJson.encodeToString(body)).ensureCommandSucceeded(path)
    }

    private suspend fun deleteCommand(path: String) {
        delete(path).ensureCommandSucceeded(path)
    }

    private suspend fun HttpResponse.ensureCommandSucceeded(path: String) {
        if (!status.isSuccess()) {
            throw TripCommandException("Command to $path failed with status ${status.value}: ${bodyAsText()}")
        }
    }
}
