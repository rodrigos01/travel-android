package travel.vola.android.ui.triplist.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.model.data.Trip
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.triplist.TripListUseCase
import kotlin.math.min

const val MAX_ITEMS_PER_LINE = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripList(
    state: TripListUseCase.State,
    onTripClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 380.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        items(state.trips) { trip ->
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
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
                flights = emptyList(),
                lodgings = emptyList(),
                places = emptyList(),
                restaurants = emptyList(),
            )
        }
    )
    AppTheme {
        TripList(state, onTripClicked = {})
    }
}