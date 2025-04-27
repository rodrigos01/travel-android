package travel.vola.android.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import travel.vola.android.di.ServiceLocator
import travel.vola.android.extensions.viewModel
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearch
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.lodgingsearch.viewmodel.LodgingSearchViewModel
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.eventlist.composable.TripDetails
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import travel.vola.android.ui.triplist.TripListViewModel
import travel.vola.android.ui.triplist.composable.TripList
import travel.vola.android.ui.triplist.composable.TripListDestination

lateinit var applicationContext: Context
    private set

private fun setApplicationContext(context: Context) {
    applicationContext = context
}

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val serviceLocator: ServiceLocator by lazy { ServiceLocator() }

    private val viewModel: StartupViewModel by viewModels(factoryProducer = { StartupViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        setApplicationContext(this.applicationContext)
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
        AppTheme(dynamicColor = false) {
            NavHost(navController = navController, startDestination = TripListDestination.ROUTE) {
                composable(TripListDestination.ROUTE) {
                    val viewModel: TripListViewModel = viewModel {
                        TripListViewModel(serviceLocator.tripRepository, navController)
                    }
                    TripList(viewModel = viewModel, navController = navController)
                }
                composable(
                    TripDetailsDestination.ROUTE, arguments = listOf(navArgument(
                        TripDetailsDestination.ARG_TRIP_ID
                    ) { type = NavType.StringType })
                ) {
                    val tripId = it.arguments?.getString(
                        TripDetailsDestination.ARG_TRIP_ID
                    ) ?: error("tripId must be provided")
                    val viewModel: travel.vola.android.ui.trip.viewmodel.TripViewModel = viewModel {
                        travel.vola.android.ui.trip.viewmodel.TripViewModel(
                            serviceLocator, navController, tripId
                        )
                    }
                    TripDetails(viewModel = viewModel, navController = navController)
                }
                composable<LodgingSearchDestination.Params> { backStackEntry ->
                    val params: LodgingSearchDestination.Params = backStackEntry.toRoute()
                    val viewModel: LodgingSearchViewModel = viewModel {
                        LodgingSearchViewModel(
                            params.tripId,
                            LodgingSearchRepository(),
                            serviceLocator.tripRepository,
                            serviceLocator.placeRepository,
                            params.locationId,
                            params.checkIn,
                            params.checkOut,
                            params.timeZoneId,
                        )
                    }
                    LodgingSearch(navController, viewModel)
                }
            }
        }
    }
}
