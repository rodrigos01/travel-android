package travel.vola.android.ui.triplist.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.model.data.Trip
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.triplist.TripListUseCase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripList(
    state: TripListUseCase.State,
    onTripClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 380.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        items(state.trips) { trip ->
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.large)
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
                preferences = null,
                flights = emptyList(),
                lodgings = emptyList(),
                places = emptyList(),
                restaurants = emptyList(),
                flexibleSections = emptyList(),
            )
        },
    )
    AppTheme {
        TripList(state, onTripClicked = {})
    }
}
