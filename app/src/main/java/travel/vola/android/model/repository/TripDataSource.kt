package travel.vola.android.model.repository

import kotlinx.coroutines.flow.Flow
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripPreferences

interface TripDataSource {

    val dataSourceType: DataSourceType
    val trips: Flow<List<Trip>>

    fun findTripById(tripId: String): Flow<Trip>
    fun getTripFlights(tripId: String): Flow<List<Flight>>
    fun getTripHotels(tripId: String): Flow<List<Lodging>>

    suspend fun addTrip(): String
    suspend fun addTrip(
        name: String,
        places: List<TimedPlace>,
        preferences: TripPreferences,
    ): String

    suspend fun updateName(tripId: String, newName: String)
    suspend fun updateTripPreferences(tripId: String, preferences: TripPreferences)
    suspend fun deleteTrip(tripId: String)
    suspend fun saveFlight(tripId: String, flight: Flight)
    suspend fun saveLodging(tripId: String, lodging: Lodging)
    suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace)
    suspend fun saveRestaurantReservation(
        tripId: String,
        restaurantReservation: RestaurantReservation,
    )

    suspend fun deleteFlight(tripId: String, flightId: String)
    suspend fun deleteLodging(tripId: String, lodgingId: String)
    suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String)
    suspend fun deleteRestaurantReservation(tripId: String, restaurantReservationId: String)
}