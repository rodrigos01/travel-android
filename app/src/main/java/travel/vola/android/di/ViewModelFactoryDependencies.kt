package travel.vola.android.di

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.firebase.FirebaseTripRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.room.RoomTripRepository
import travel.vola.android.model.room.TravelDatabase

private val FACTORY_DEPENDENCIES_KEY = CreationExtras.Key<ViewModelFactoryDependencies>()

class ViewModelFactoryDependencies(
    val navController: NavController,
    val dataSourceType: DataSourceType,
    getApplicationContext: () -> Context
) {

    private val roomTripRepository by lazy {
        RoomTripRepository(
            Room.databaseBuilder(
                getApplicationContext(),
                TravelDatabase::class.java,
                "travel-db",
            ).build().tripDao
        )
    }

    private val firebaseTripRepository by lazy {
        FirebaseTripRepository(FirebaseFirestore.getInstance())
    }

    val tripRepository: TripRepository by lazy {
        when (dataSourceType) {
            DataSourceType.LOCAL -> roomTripRepository
            DataSourceType.FIREBASE -> firebaseTripRepository
        }
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }
}

fun ComponentActivity.initializeViewModelCreationExtras(
    navController: NavController,
    dataSourceType: DataSourceType,
): CreationExtras =
    MutableCreationExtras().also { extras ->
        extras[FACTORY_DEPENDENCIES_KEY] =
            ViewModelFactoryDependencies(navController, dataSourceType, ::getApplicationContext)
    }

val LocalViewModelCreationExtras = staticCompositionLocalOf<CreationExtras> { CreationExtras.Empty }

val CreationExtras.factoryDependencies: ViewModelFactoryDependencies
    get() = get(FACTORY_DEPENDENCIES_KEY) ?: error("No factory dependencies found")
