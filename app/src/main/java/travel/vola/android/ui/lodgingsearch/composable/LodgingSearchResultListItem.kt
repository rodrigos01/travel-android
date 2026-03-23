package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import travel.vola.android.common.ui.components.asSizedImageTarget
import travel.vola.android.common.ui.components.rememberSizedImageState
import travel.vola.android.ui.lodgingsearch.state.LodgingSearchResultState

enum class LodgingSearchResultListItemStyle {
    Expanded,
    Compact,
}

@Composable
fun LodgingSearchResultListItem(
    result: LodgingSearchResultState,
    modifier: Modifier = Modifier,
    style: LodgingSearchResultListItemStyle = LodgingSearchResultListItemStyle.Expanded,
) {
    when (style) {
        LodgingSearchResultListItemStyle.Compact -> CompactLodgingSearchResultListItem(
            result,
            modifier = modifier,
        )

        LodgingSearchResultListItemStyle.Expanded -> ExpandedLodgingSearchResultListItem(
            result,
            modifier = modifier,
        )
    }
}

@Composable
private fun CompactLodgingSearchResultListItem(
    result: LodgingSearchResultState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        val sizedImageState = rememberSizedImageState(result.coverImage)
        Image(
            painter = rememberAsyncImagePainter(sizedImageState.model),
            contentDescription = "Place Description",
            modifier = Modifier
                .weight(1 / 3F)
                .background(color = MaterialTheme.colorScheme.tertiary)
                .asSizedImageTarget(sizedImageState),
            contentScale = ContentScale.Crop,
        )
        ItemContent(
            result,
            modifier = Modifier
                .weight(2 / 3F)
                .wrapContentHeight()
                .padding(all = 16.dp),
        )
    }
}

@Composable
private fun ExpandedLodgingSearchResultListItem(
    result: LodgingSearchResultState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 16.dp),
    ) {
        val sizedImageState = rememberSizedImageState(result.coverImage)
        Image(
            painter = rememberAsyncImagePainter(sizedImageState.model),
            contentDescription = "Place Description",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f)
                .background(color = MaterialTheme.colorScheme.tertiary)
                .asSizedImageTarget(sizedImageState),
            contentScale = ContentScale.Crop,
        )
        ItemContent(result, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun ItemContent(result: LodgingSearchResultState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(result.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            result.address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            LodgingRating(result.rating)
            Text(
                result.lodgingType,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            PriceText(result.price)
        }
    }
}

@Preview
@Composable
private fun LodgingSearchResultListItemPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val state = LodgingSearchResultState(
            id = "1",
            name = "Hotel Number 1",
            address = "123 Main St, Anytown USA",
            coverImage = "https://picsum.photos/200",
            price = 100.0,
            rating = 4.5,
            reviewCount = 16543,
            lodgingType = "Hotel",
            latitude = 0.0,
            longitude = 0.0,
        )
        LodgingSearchResultListItem(
            state,
            style = LodgingSearchResultListItemStyle.Compact,
        )
        LodgingSearchResultListItem(
            state,
            style = LodgingSearchResultListItemStyle.Expanded,
        )
    }
}
