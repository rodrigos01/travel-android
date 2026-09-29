package travel.vola.android.ui

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import travel.vola.android.common.ui.components.OverlayHostProvider
import travel.vola.android.di.LocalViewModelCreationExtras
import travel.vola.android.di.initializeViewModelCreationExtras
import travel.vola.android.ui.home.HomeScreen
import travel.vola.android.ui.home.HomeScreenDestination
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearch
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.signin.SignInScreen
import travel.vola.android.ui.signin.SignInScreenDestination
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.composable.TripCreationAssistant
import travel.vola.android.ui.trip.creation.assistant.composable.TripCreationAssistantDestination
import travel.vola.android.ui.trip.eventlist.composable.TripDetails
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val viewModel: StartupViewModel by viewModels(factoryProducer = { StartupViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MainScreen() }
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (viewModel.uiState.value.isLoaded) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                } else {
                    return false
                }
            }
        })
    }

    @ExperimentalMaterial3Api
    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        AppTheme {
            val viewModelCreationExtras = remember {
                initializeViewModelCreationExtras(navController)
            }
            OverlayHostProvider {
                CompositionLocalProvider(LocalViewModelCreationExtras provides viewModelCreationExtras) {
                    val startDestination = remember {
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            HomeScreenDestination.ROUTE
                        } else {
                            SignInScreenDestination.ROUTE
                        }
                    }
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable(SignInScreenDestination.ROUTE) {
                            SignInScreen()
                        }
                        composable(HomeScreenDestination.ROUTE) {
                            HomeScreen(navController)
                        }
                        composable(TripCreationAssistantDestination.ROUTE) {
                            TripCreationAssistant(TripCreationAssistantDestination.Params())
                        }
                        composable<TripCreationAssistantDestination.Params> {
                            val params: TripCreationAssistantDestination.Params = it.toRoute()
                            TripCreationAssistant(params)
                        }
                        composable(
                            TripDetailsDestination.ROUTE,
                            arguments = listOf(
                                navArgument(
                                    TripDetailsDestination.ARG_TRIP_ID,
                                ) { type = NavType.StringType },
                            ),
                        ) {
                            val tripId = it.arguments?.getString(
                                TripDetailsDestination.ARG_TRIP_ID,
                            ) ?: error("tripId must be provided")
                            TripDetails(tripId, navController)
                        }
                        composable<LodgingSearchDestination.Params> { backStackEntry ->
                            val params: LodgingSearchDestination.Params = backStackEntry.toRoute()
                            LodgingSearch(params, navController)
                        }
                    }
                }
            }
        }
    }
}
