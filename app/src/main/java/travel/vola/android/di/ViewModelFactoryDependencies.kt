package travel.vola.android.di

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.firebase.FirebaseTripDataSource
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.network.ApiTripCommandDataSource
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.repository.TripRepositoryImpl

private val FACTORY_DEPENDENCIES_KEY = CreationExtras.Key<ViewModelFactoryDependencies>()

class ViewModelFactoryDependencies(
    val navController: NavController,
) {

    private val firebaseTripDataSource by lazy {
        FirebaseTripDataSource(FirebaseFirestore.getInstance())
    }

    private val apiTripCommandDataSource by lazy {
        ApiTripCommandDataSource()
    }

    val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(firebaseTripDataSource, apiTripCommandDataSource)
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
        ViewModelFactoryDependencies(navController)
}

val LocalViewModelCreationExtras = staticCompositionLocalOf<CreationExtras> { CreationExtras.Empty }

val CreationExtras.factoryDependencies: ViewModelFactoryDependencies
    get() = get(FACTORY_DEPENDENCIES_KEY) ?: error("No factory dependencies found")
