package com.combah.travel2.ui.triplist.composable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.combah.travel2.model.repository.mock.MockTripRepository
import com.combah.travel2.ui.trip.eventlist.composable.TripDetailsDestination
import com.combah.travel2.ui.triplist.TripListViewModel

@Composable
fun TripList(viewModel: TripListViewModel, navController: NavController) {
    val trips by viewModel.trips.observeAsState(emptyList())
    LazyColumn {
        items(trips) { trip ->
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                onClick = { navController.navigate(TripDetailsDestination.getRoute(trip.id)) })
        }
    }
}

@Composable
@Preview
fun TripListPreview() {
    MaterialTheme {
        TripList(TripListViewModel(MockTripRepository()), rememberNavController())
    }
}

object TripListDestination {
    const val ROUTE = "trip_list"
}