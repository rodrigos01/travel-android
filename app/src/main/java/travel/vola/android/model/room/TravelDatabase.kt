package travel.vola.android.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RoomData.Schema.Trip::class, RoomData.Schema.Flight::class, RoomData.Schema.FlightSegment::class, RoomData.Schema.Airport::class, RoomData.Schema.Lodging::class, RoomData.Schema.TimedPlace::class, RoomData.Place::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {
    abstract val tripDao: TripDao
}