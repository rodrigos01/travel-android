package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import travel.vola.android.common.ui.preview.loremIpsum
import travel.vola.android.extensions.Time
import travel.vola.android.ui.lodgingsearch.state.LodgingReviewState
import travel.vola.android.ui.theme.AppTheme

@Composable
fun LodgingReviewItem(state: LodgingReviewState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LodgingRating(state.rating)
            Text(state.title, style = MaterialTheme.typography.titleMedium)
        }
        var expanded by remember { mutableStateOf(true) }
        var hasMoreText by remember { mutableStateOf(false) }
        Text(
            state.review,
            maxLines = if (expanded) Int.MAX_VALUE else 6,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                if (!hasMoreText && it.lineCount > 20) {
                    hasMoreText = true
                    expanded = false
                }
            },
            modifier = Modifier.animateContentSize(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(ButtonDefaults.MinHeight)
        ) {
            Image(
                painter = rememberAsyncImagePainter(state.authorAvatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Text(state.authorName, style = MaterialTheme.typography.labelLarge)
            state.authorLocation?.let { location ->
                Text(
                    location,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (hasMoreText) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { expanded = !expanded },
                ) {
                    Text(if (expanded) "Read less" else "Read more")
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun LodgingReviewItemPreview() {
    AppTheme {
        Surface {
            LodgingReviewItem(
                state = LodgingReviewState(
                    rating = 4.5,
                    ratingImageUrl = "",
                    tripDate = Time("2023-08-15T00:00 GMT"),
                    reviewTime = Time("2023-08-31T10:52 GMT"),
                    authorAvatarUrl = null,
                    authorName = "Author",
                    authorLocation = "Author Location",
                    review = loremIpsum(),
                    title = "A lovely stay",
                )
            )
        }
    }
}