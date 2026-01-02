package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import travel.vola.android.common.ui.components.placeholderPainter
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaceDetailsListItem(
    state: TripItemState.PlaceDetailsItemState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = state.imageUrl,
            placeholder = placeholderPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .aspectRatio(21 / 9F),
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                state.name,
                style = MaterialTheme.typography.bodyLarge,

                )
            Text(
                state.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            state.note,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )
    }
}

@Preview
@Composable
fun PlaceDetailsListItemPreview() {
    AppTheme {
        PlaceDetailsListItem(
            TripItemState.PlaceDetailsItemState(
                "Place Name",
                "Place address",
                "",
                "A nice place to visit at some point in your trip",
            )
        )
    }
}