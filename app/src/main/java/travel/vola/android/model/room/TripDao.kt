package travel.vola.android.model.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import travel.vola.android.model.room.RoomData.Schema
import travel.vola.android.model.room.RoomData.Trip

@Dao
interface TripDao {
    @Transaction
    @Query("SELECT * FROM trip")
    fun observeTrips(): Flow<List<Trip>>

    @Transaction
    @Query("SELECT * FROM trip WHERE id = :tripId")
    fun observeTrip(tripId: String): Flow<Trip?>

    @Query("SELECT * FROM trip WHERE id = :tripId")
    suspend fun getTrip(tripId: String): Schema.Trip

    @Query("SELECT * FROM flight WHERE tripId = :tripId AND id = :flightId")
    suspend fun getFlight(tripId: String, flightId: String): Schema.Flight

    @Query("SELECT * FROM lodging WHERE tripId = :tripId AND id = :lodgingId")
    suspend fun getLodging(tripId: String, lodgingId: String): Schema.Lodging

    @Query("SELECT * FROM timedPlace WHERE tripId = :tripId AND id = :timedPlaceId")
    suspend fun getTimedPlace(tripId: String, timedPlaceId: String): Schema.TimedPlace

    @Query("SELECT * FROM restaurantReservation WHERE tripId = :tripId AND id = :restaurantReservationId")
    suspend fun getRestaurantReservation(
        tripId: String,
        restaurantReservationId: String,
    ): Schema.RestaurantReservation

    @Query("SELECT * FROM flexibleSection WHERE tripId = :tripId AND id = :flexibleSectionId")
    suspend fun getFlexibleSection(
        tripId: String,
        flexibleSectionId: String,
    ): Schema.FlexibleSection

    @Insert
    suspend fun addTrip(trip: Schema.Trip)

    @Update
    suspend fun updateTrip(trip: Schema.Trip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlight(flight: Schema.Flight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlightSegment(flightSegment: Schema.FlightSegment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAirport(airport: Schema.Airport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLodging(lodging: Schema.Lodging)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimedPlace(timedPlace: Schema.TimedPlace)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRestaurantReservation(restaurantReservation: Schema.RestaurantReservation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlace(place: RoomData.Place)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlexibleSection(flexibleSection: Schema.FlexibleSection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlexibleSectionCategory(category: Schema.FlexibleSectionCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlexibleSectionItem(item: Schema.FlexibleSectionItem)

    @Delete
    suspend fun deleteTrip(trip: Schema.Trip)

    @Delete
    suspend fun deleteFlight(flight: Schema.Flight)

    @Delete
    suspend fun deleteLodging(lodging: Schema.Lodging)

    @Delete
    suspend fun deleteTimedPlace(timedPlace: Schema.TimedPlace)

    @Delete
    suspend fun deleteRestaurantReservation(restaurantReservation: Schema.RestaurantReservation)

    @Delete
    suspend fun deleteFlexibleSection(flexibleSection: Schema.FlexibleSection)
}