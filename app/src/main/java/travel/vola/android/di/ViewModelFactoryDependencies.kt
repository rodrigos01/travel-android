package travel.vola.android.di

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import androidx.room.Room
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.room.RoomTripRepository
import travel.vola.android.model.room.TravelDatabase

private val FACTORY_DEPENDENCIES_KEY = CreationExtras.Key<ViewModelFactoryDependencies>()

class ViewModelFactoryDependencies(
    val navController: NavController,
    getApplicationContext: () -> Context
) {

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

fun ComponentActivity.initializeViewModelCreationExtras(navController: NavController): CreationExtras =
    MutableCreationExtras().also { extras ->
        extras[FACTORY_DEPENDENCIES_KEY] =
            ViewModelFactoryDependencies(navController, ::getApplicationContext)
    }

val LocalViewModelCreationExtras = staticCompositionLocalOf<CreationExtras> { CreationExtras.Empty }

val CreationExtras.factoryDependencies: ViewModelFactoryDependencies
    get() = get(FACTORY_DEPENDENCIES_KEY) ?: error("No factory dependencies found")
