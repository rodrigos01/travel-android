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
    fun observeTrip(tripId: String): Flow<Trip>

    @Query("SELECT * FROM trip WHERE id = :tripId")
    suspend fun getTrip(tripId: String): Schema.Trip

    @Query("SELECT * FROM flight WHERE tripId = :tripId AND id = :flightId")
    suspend fun getFlight(tripId: String, flightId: String): Schema.Flight

    @Query("SELECT * FROM lodging WHERE tripId = :tripId AND id = :lodgingId")
    suspend fun getLodging(tripId: String, lodgingId: String): Schema.Lodging

    @Query("SELECT * FROM timedPlace WHERE tripId = :tripId AND id = :timedPlaceId")
    suspend fun getTimedPlace(tripId: String, timedPlaceId: String): Schema.TimedPlace

    @Insert
    suspend fun addTrip(trip: Schema.Trip)

    @Update
    suspend fun updateTrip(trip: Schema.Trip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlight(flight: Schema.Flight)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLodging(lodging: Schema.Lodging)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimedPlace(timedPlace: Schema.TimedPlace)

    @Delete
    suspend fun deleteTrip(trip: Schema.Trip)

    @Delete
    suspend fun deleteFlight(flight: Schema.Flight)

    @Delete
    suspend fun deleteLodging(lodging: Schema.Lodging)

    @Delete
    suspend fun deleteTimedPlace(timedPlace: Schema.TimedPlace)
}