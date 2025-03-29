package travel.vola.android.ui.triplist.composable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vola.android.model.repository.mock.MockTripRepository
import com.vola.android.ui.theme.AppTheme
import com.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import com.vola.android.ui.triplist.TripListViewModel

@Composable
fun TripList(viewModel: TripListViewModel, navController: NavController) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addTrip() }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(state.trips) { trip ->
                TripListItem(
                    name = trip.name,
                    coverImageUrl = trip.coverImage,
                    onClick = { navController.navigate(TripDetailsDestination.getRoute(trip.id)) })
            }
        }
    }
}

@Composable
@Preview
fun TripListPreview() {
    val navController = rememberNavController()
    AppTheme {
        TripList(TripListViewModel(MockTripRepository(), navController), navController)
    }
}

object TripListDestination {
    const val ROUTE = "trip_list"
}