package travel.vola.android.ui.triplist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import travel.vola.android.ui.theme.AppTheme

@Composable
fun TripListItem(name: String?, coverImageUrl: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        AsyncImage(
            model = coverImageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f)
                .then(
                    if (LocalInspectionMode.current) {
                        Modifier.background(MaterialTheme.colorScheme.tertiary)
                    } else {
                        Modifier
                    }
                ),
            contentDescription = "Place Description",
            contentScale = ContentScale.Crop
        )
        Text(
            text = name ?: "",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
@Preview
fun EventListItemPreview() {
    AppTheme {
        TripListItem(
            name = "Trip to Barcelona, Paris, Grindewald and Zurich",
            coverImageUrl = "",
        )
    }
}