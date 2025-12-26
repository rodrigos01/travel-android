package travel.vola.android.model.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(
    entities = [RoomData.Schema.Trip::class, RoomData.Schema.Flight::class, RoomData.Schema.FlightSegment::class, RoomData.Schema.Airport::class, RoomData.Schema.Lodging::class, RoomData.Schema.TimedPlace::class, RoomData.Place::class, RoomData.Schema.RestaurantReservation::class, RoomData.Schema.FlexibleSection::class, RoomData.Schema.FlexibleSectionCategory::class, RoomData.Schema.FlexibleSectionItem::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {
    abstract val tripDao: TripDao
}

fun buildDatabase(context: Context): TravelDatabase =
    Room.databaseBuilder(
        context,
        TravelDatabase::class.java,
        "travel-db",
    ).addMigrations(
        Migration(2, 3) {
            it.execSQL("ALTER TABLE place ADD COLUMN timeZone TEXT")
        },
        Migration(3, 4) {
            it.execSQL("ALTER TABLE trip ADD COLUMN preferences TEXT")
        }
    )
        .build()