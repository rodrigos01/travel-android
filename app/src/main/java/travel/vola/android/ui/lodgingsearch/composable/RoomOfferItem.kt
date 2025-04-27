package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import travel.vola.android.R
import travel.vola.android.common.ui.components.asSizedImageTarget
import travel.vola.android.common.ui.components.rememberSizedImageState
import travel.vola.android.ui.lodgingsearch.state.LodgingRoomOfferState

@Composable
fun RoomOfferItem(
    state: LodgingRoomOfferState,
    onCoverImageTapped: (String) -> Unit,
    onViewOfferTapped: () -> Unit
) {
    Row(
        horizontalArrangement = spacedBy(8.dp), modifier = Modifier.fillMaxWidth()
    ) {
        val roomCoverPhoto = state.photos.firstOrNull()
        if (roomCoverPhoto != null) {
            val sizedImageState = rememberSizedImageState(roomCoverPhoto)
            LodgingImage(
                rememberAsyncImagePainter(
                    model = sizedImageState.model, contentScale = ContentScale.Crop
                ),
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onCoverImageTapped(roomCoverPhoto) }
                    .asSizedImageTarget(sizedImageState),
            )
        } else {
            LodgingImage(
                painterResource(R.drawable.hotel_baseline_24),
                contentScale = ContentScale.None,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.3F
                    )
                ),
                modifier = Modifier.size(64.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1F)
                .align(Alignment.Top)
        ) {
            val features = listOf(
                "Breakfast Included" to state.breakfastIncluded,
                "Refundable" to state.refundable,
                "No pre-payment required" to !state.prePaymentRequired,
                "All inclusive" to state.isAllInclusive,
            ).filter { it.second }.map { it.first }
            Text(
                state.description,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            features.forEach { feature ->
                Text(
                    text = AnnotatedString.Builder().apply {
                        append("\u2022")
                        append("\u0009")
                        append(feature)
                    }.toAnnotatedString(), style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(112.dp)
        ) {
            PriceText(state.price)
            TextButton(onClick = onViewOfferTapped) {
                ButtonContent(
                    iconResId = R.drawable.open_in_new_outline_24,
                    iconContentDescription = "Open offer button icon",
                    text = state.bookingAgency
                )
            }
        }
    }
}