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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import travel.vola.android.di.LocalViewModelCreationExtras
import travel.vola.android.di.initializeViewModelCreationExtras
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.ui.home.HomeScreen
import travel.vola.android.ui.home.HomeScreenDestination
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearch
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.eventlist.composable.TripDetails
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination

lateinit var applicationContext: Context
    private set

private fun setApplicationContext(context: Context) {
    applicationContext = context
}

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

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
            var dataSourceType by remember { mutableStateOf(DataSourceType.LOCAL) }
            val viewModelCreationExtras = remember(dataSourceType) {
                initializeViewModelCreationExtras(
                    navController,
                    dataSourceType
                )
            }
            CompositionLocalProvider(LocalViewModelCreationExtras provides viewModelCreationExtras) {
                NavHost(
                    navController = navController, startDestination = HomeScreenDestination.ROUTE
                ) {
                    composable(HomeScreenDestination.ROUTE) {
                        HomeScreen(navController, onDataSourceTypeChanged = {
                            dataSourceType = it
                        })
                    }
                    composable(
                        TripDetailsDestination.ROUTE, arguments = listOf(
                            navArgument(
                                TripDetailsDestination.ARG_TRIP_ID
                            ) { type = NavType.StringType })
                    ) {
                        val tripId = it.arguments?.getString(
                            TripDetailsDestination.ARG_TRIP_ID
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
