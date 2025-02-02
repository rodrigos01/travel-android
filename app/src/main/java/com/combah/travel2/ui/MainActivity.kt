package com.combah.travel2.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.combah.travel2.di.ServiceLocator
import com.combah.travel2.extensions.viewModel
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.eventlist.composable.TripDetails
import com.combah.travel2.ui.trip.eventlist.composable.TripDetailsDestination
import com.combah.travel2.ui.trip.viewmodel.TripViewModel
import com.combah.travel2.ui.triplist.TripListViewModel
import com.combah.travel2.ui.triplist.composable.TripList
import com.combah.travel2.ui.triplist.composable.TripListDestination

@ExperimentalMaterial3Api
class MainActivity : AppCompatActivity() {

    private val serviceLocator: ServiceLocator by lazy { ServiceLocator() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MainScreen() }
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
                    val viewModel: TripViewModel = viewModel {
                        TripViewModel(
                            serviceLocator, tripId
                        )
                    }
                    TripDetails(viewModel = viewModel, navController = navController)
                }
            }
        }
    }
}
