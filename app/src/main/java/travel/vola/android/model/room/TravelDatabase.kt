package travel.vola.android.model.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomData.Trip::class], version = 1)
abstract class TravelDatabase : RoomDatabase() {
    abstract val tripDao: TripDao
}