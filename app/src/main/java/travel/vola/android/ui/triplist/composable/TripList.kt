package travel.vola.android.ui.triplist.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import travel.vola.android.extensions.viewModel
import travel.vola.android.model.data.Trip
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import travel.vola.android.ui.triplist.TripListUseCase
import travel.vola.android.ui.triplist.TripListViewModel
import kotlin.math.min

const val MAX_ITEMS_PER_LINE = 3

@Composable
fun TripList(
    navController: NavController,
) {
    val viewModel: TripListViewModel = viewModel(
        factory = TripListViewModel.Factory()
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    TripList(
        state = state,
        onAddTrip = { viewModel.addTrip() },
        onTripClicked = { tripId ->
            navController.navigate(
                TripDetailsDestination.getRoute(
                    tripId
                )
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripList(
    state: TripListUseCase.State,
    onAddTrip: () -> Unit,
    onTripClicked: (String) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrip) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    ) { paddingValues ->
        var smallerWidth by remember { mutableIntStateOf(Int.MAX_VALUE) }
        ContextualFlowRow(
            itemCount = state.trips.size,
            maxItemsInEachRow = MAX_ITEMS_PER_LINE,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 16.dp,
                ),
        ) { index ->
            val trip = state.trips[index]
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                modifier = Modifier
                    .weight(1F)
                    .widthIn(min = 260.dp, max = with(LocalDensity.current) { smallerWidth.toDp() })
                    .heightIn(min = 280.dp)
                    .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
                    .onPlaced {
                        smallerWidth = min(smallerWidth, it.size.width)
                    }
                    .clickable {
                        onTripClicked(trip.id)
                    },
            )
        }
    }
}

@Composable
@Preview
@Preview(device = "spec:parent=pixel_tablet,orientation=portrait")
@Preview(device = "id:pixel_tablet")
fun TripListPreview() {
    val state = TripListUseCase.State(
        trips = List(7) { index ->
            Trip(
                id = "$index",
                name = "Trip $index",
                coverImage = "",
                flights = emptyList(),
                lodgings = emptyList(),
                places = emptyList(),
            )
        }
    )
    AppTheme {
        TripList(state, onTripClicked = {}, onAddTrip = {})
    }
}

object TripListDestination {
    const val ROUTE = "trip_list"
}