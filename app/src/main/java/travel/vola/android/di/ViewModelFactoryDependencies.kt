package travel.vola.android.di

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.firebase.FirebaseTripDataSource
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.repository.AuthRepository
import travel.vola.android.model.repository.CredentialManagerGoogleIdTokenProvider
import travel.vola.android.model.repository.GoogleIdTokenProvider
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.repository.TripRepositoryImpl

private val FACTORY_DEPENDENCIES_KEY = CreationExtras.Key<ViewModelFactoryDependencies>()

class ViewModelFactoryDependencies(
    val navController: NavController,
    private val activityContext: Context,
) {

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Reads currentUser lazily (via FirebaseTripDataSource's getter, not at
    // construction time) so a sign-out followed by a different user signing
    // back in within the same app session still queries the right owner.
    private val firebaseTripDataSource by lazy {
        FirebaseTripDataSource(FirebaseFirestore.getInstance(), firebaseAuth)
    }

    val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(firebaseTripDataSource)
    }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository()
    }

    val genAIRepository: GenAIRepository by lazy {
        GenAIRepository()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth)
    }

    val googleIdTokenProvider: GoogleIdTokenProvider by lazy {
        CredentialManagerGoogleIdTokenProvider(activityContext)
    }
}

fun ComponentActivity.initializeViewModelCreationExtras(
    navController: NavController,
): CreationExtras = MutableCreationExtras().also { extras ->
    extras[FACTORY_DEPENDENCIES_KEY] =
        ViewModelFactoryDependencies(navController, activityContext = this)
}

val LocalViewModelCreationExtras = staticCompositionLocalOf<CreationExtras> { CreationExtras.Empty }

val CreationExtras.factoryDependencies: ViewModelFactoryDependencies
    get() = get(FACTORY_DEPENDENCIES_KEY) ?: error("No factory dependencies found")
