package travel.vola.android.di

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.datastore.DataStoreUserPreferencesRepository
import travel.vola.android.model.datastore.userPreferencesDataStore
import travel.vola.android.model.firebase.FirebaseTripDataSource
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.multisource.MultiSourceTripRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.room.RoomTripDataSource
import travel.vola.android.model.room.buildDatabase

private val FACTORY_DEPENDENCIES_KEY = CreationExtras.Key<ViewModelFactoryDependencies>()

class ViewModelFactoryDependencies(
    val navController: NavController, getApplicationContext: () -> Context
) {

    private val roomTripDataSource by lazy {
        RoomTripDataSource(
            buildDatabase(getApplicationContext()).tripDao
        )
    }

    private val firebaseTripDataSource by lazy {
        FirebaseTripDataSource(FirebaseFirestore.getInstance())
    }

    val userPreferencesRepository by lazy {
        DataStoreUserPreferencesRepository(
            dataStore = getApplicationContext().userPreferencesDataStore,
        )
    }

    private val multiSourceTripRepository by lazy {
        MultiSourceTripRepository(
            userPreferencesRepository = userPreferencesRepository,
            roomTripDataSource,
            firebaseTripDataSource
        )
    }

    val tripRepository: TripRepository by lazy {
        multiSourceTripRepository
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }

    val genAIRepository: GenAIRepository by lazy {
        GenAIRepository()
    }
}

fun ComponentActivity.initializeViewModelCreationExtras(
    navController: NavController,
): CreationExtras = MutableCreationExtras().also { extras ->
    extras[FACTORY_DEPENDENCIES_KEY] =
        ViewModelFactoryDependencies(navController, ::getApplicationContext)
}

val LocalViewModelCreationExtras = staticCompositionLocalOf<CreationExtras> { CreationExtras.Empty }

val CreationExtras.factoryDependencies: ViewModelFactoryDependencies
    get() = get(FACTORY_DEPENDENCIES_KEY) ?: error("No factory dependencies found")
