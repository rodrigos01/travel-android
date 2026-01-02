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
import travel.vola.android.common.ui.components.rememberPlaceholderPainter
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState


@Composable
fun PlaceDetailsListItem(
    state: TripItemState.PlaceDetailsItemState,
    modifier: Modifier = Modifier,
) {
    PlaceDetailsListItem(
        title = state.name,
        subtitle = state.subtitle,
        imageUrl = state.imageUrl,
        note = state.note,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaceDetailsListItem(
    title: String,
    subtitle: String,
    imageUrl: String,
    note: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            placeholder = rememberPlaceholderPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .aspectRatio(16 / 9F),
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,

                )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                note,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
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
            ), modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}