package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.PlaceEvent
import com.combah.travel2.ui.theme.AppTheme
import java.util.Date

@Composable
fun PlaceEventListItem(event: PlaceEvent) {
    BoxWithConstraints {
        AsyncImage(
            model = event.currentPlace.coverImage,
            contentDescription = "Place Description",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.33f),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = event.place.name,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
@Preview
fun PlaceEventListItemPreview() {
    AppTheme {
        PlaceEventListItem(event = PlaceEvent(Place(name = "New York City"), arrival = Date()))
    }
}