package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import travel.vola.android.common.ui.preview.loremIpsum
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.monthAndYearString
import travel.vola.android.ui.lodgingsearch.state.LodgingReviewState
import travel.vola.android.ui.theme.AppTheme
import java.time.format.FormatStyle

@Composable
fun LodgingReviewItem(state: LodgingReviewState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    state.reviewTime.dateString(style = FormatStyle.LONG),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(state.ratingImageUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier.height(24.dp)
            )
        }
        var expanded by remember { mutableStateOf(true) }
        var hasMoreText by remember { mutableStateOf(false) }
        Text(
            state.review,
            maxLines = if (expanded) Int.MAX_VALUE else 6,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                if (!hasMoreText && it.lineCount > 6) {
                    hasMoreText = true
                    expanded = false
                }
            },
            modifier = Modifier.animateContentSize(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(min = ButtonDefaults.MinHeight)
        ) {
            Image(
                painter = rememberAsyncImagePainter(state.authorAvatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.authorName, style = MaterialTheme.typography.labelLarge)
                    state.authorLocation?.let { location ->
                        Text(
                            location,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Text(
                    "Traveled on ${state.tripDate.monthAndYearString}",
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
                    ratingImageUrl = "https://www.tripadvisor.com/img/cdsi/img2/ratings/traveler/s5.0-66827-5.svg",
                    tripDate = zonedDateTime("2023-08-15T00:00 GMT"),
                    reviewTime = zonedDateTime("2023-08-31T10:52 GMT"),
                    authorAvatarUrl = "https://media-cdn.tripadvisor.com/media/photo-l/1a/f6/e4/2d/default-avatar-2020-48.jpg",
                    authorName = "Author",
                    authorLocation = "Author Location",
                    review = loremIpsum(),
                    title = "A Super long review title that will span multiple lines for sure",
                )
            )
        }
    }
}
