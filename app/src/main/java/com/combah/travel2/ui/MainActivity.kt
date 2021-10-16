package com.combah.travel2.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.combah.travel2.ui.trip.TripViewModel
import com.combah.travel2.ui.trip.creation.TransportationSetupViewModel
import com.combah.travel2.ui.trip.creation.composable.TransportationSetup
import com.combah.travel2.ui.trip.creation.composable.TransportationSetupDestination
import com.combah.travel2.ui.trip.eventlist.composable.TripDetails
import com.combah.travel2.ui.trip.eventlist.composable.TripDetailsDestination
import com.combah.travel2.ui.triplist.TripListViewModel
import com.combah.travel2.ui.triplist.composable.TripList
import com.combah.travel2.ui.triplist.composable.TripListDestination
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { MainScreen() }
    }

    @ExperimentalMaterialApi
    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = TripListDestination.ROUTE) {
            composable(TripListDestination.ROUTE) {
                val viewModel: TripListViewModel by viewModel()
                TripList(viewModel = viewModel, navController = navController)
            }
            composable(
                TripDetailsDestination.ROUTE,
                arguments = listOf(navArgument(
                    TripDetailsDestination.ARG_TRIP_ID
                ) { type = NavType.StringType })
            ) { navBackStackEntry ->
                val tripId = navBackStackEntry.arguments?.getString(
                    TripDetailsDestination.ARG_TRIP_ID
                )
                val viewModel: TripViewModel by viewModel { parametersOf(tripId) }
                TripDetails(viewModel = viewModel, navController = navController)
            }
            composable(TransportationSetupDestination.KEY) {
                val viewModel: TransportationSetupViewModel by viewModel()
                TransportationSetup(viewModel = viewModel)
            }
        }
    }
}
