package travel.vola.android.di

import android.content.Context
import androidx.room.Room
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.room.RoomTripRepository
import travel.vola.android.model.room.TravelDatabase

class ServiceLocator(getApplicationContext: () -> Context) {
    val tripRepository: TripRepository by lazy {
        RoomTripRepository(
            Room.databaseBuilder(
                getApplicationContext(),
                TravelDatabase::class.java,
                "travel-db",
            ).build().tripDao
        )
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }
}