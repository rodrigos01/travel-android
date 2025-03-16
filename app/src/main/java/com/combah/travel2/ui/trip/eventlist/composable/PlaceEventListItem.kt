package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.R
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun PlaceEventListItem(imageUrl: String, placeName: String, startDate: String, endDate: String) {
    Box(
        Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.tertiary)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "Place Description",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.33f),
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3F), blendMode = BlendMode.SrcAtop)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = placeName,
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = stringResource(id = R.string.place_item_subtitle, startDate, endDate),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

}

@Composable
@Preview
fun PlaceEventListItemPreview() {
    AppTheme {
        PlaceEventListItem(
            "https://encrypted-tbn1.gstatic.com/licensed-image?q=tbn:ANd9GcRDbATmnXe3E928q15EUN0TxheIKg6QwtjNKMqMTH297oQ5AsfFKehPfGsyzbKTPrKkybBhdbp_CWtuZxf6Sx6ld8xxqYty1UaqhJfmZQw",
            "New York City",
            "May 11",
            "May 21",
        )
    }
}