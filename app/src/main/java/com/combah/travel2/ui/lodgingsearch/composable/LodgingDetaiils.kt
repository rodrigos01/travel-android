package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.R
import com.combah.travel2.extensions.Time
import com.combah.travel2.ui.lodgingsearch.state.LodgingDetailsState
import com.combah.travel2.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LodgingDetails(state: LodgingDetailsState) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(state.name)
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Close, contentDescription = "")
                        }
                    }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                    modifier = Modifier.padding(all = 16.dp)
                ) {
                    LodgingRating(state.rating)
                    Text(state.reviewCountText)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(state.lodgingType)
                }
            }
        }
    ) { paddingValues ->
        Column(verticalArrangement = spacedBy(8.dp), modifier = Modifier.padding(paddingValues)
            .padding(horizontal = 16.dp)) {
            Row(
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16F / 9F)
            ) {
                LodgingImage(
                    state.photos.first(), modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                )
                Column(
                    verticalArrangement = spacedBy(8.dp),
                ) {
                    LodgingImage(
                        state.photos[1], modifier = Modifier
                            .weight(1F)
                            .matchWidthToHeight()
                    )
                    Surface(
                        modifier = Modifier
                            .weight(1F)
                            .matchWidthToHeight(),
                        tonalElevation = 4.dp,
                        onClick = { /* show more photos */ }
                    ) {
                        LodgingImage(
                            state.photos[2]
                        )
                    }
                }
            }
            Row(horizontalArrangement = spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {/* Add to trip */}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), modifier = Modifier.weight(1F)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add button icon")
                    Text("Add to trip")
                }
                FilledTonalButton(onClick = {/* Save lodging */}) {
                    Icon(painterResource(R.drawable.outline_bookmark_border_24), contentDescription = "Save button icon")
                    Text("Save hotel")
                }
            }
        }
    }
}

@Composable
private fun LodgingImage(model: String, modifier: Modifier = Modifier) {
    Image(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        painter = rememberAsyncImagePainter(model = model),
        contentDescription = "Lodging Image Description",
        contentScale = ContentScale.FillHeight
    )
}

@Composable
private fun Int.toDp() = with(LocalDensity.current) { toDp() }

@Composable
private fun Modifier.matchWidthToHeight(): Modifier {
    var width by remember { mutableIntStateOf(0) }
    return this
        .width(width.toDp())
        .onPlaced {
            width = it.size.height
        }
}

@Preview(showBackground = true)
@Composable
fun LodgingDetailsPreview() {
    AppTheme {
        LodgingDetails(
            LodgingDetailsState(
                name = "A very long Hotel name that might span multiple lines",
                rating = 4.5,
                reviewCountText = "123 reviews",
                lodgingType = "5-Star Hotel",
                photos = List(10) { index -> "" },
                checkIn = Time("2025-08-10T00:00 -0500"),
                checkOut = Time("2025-08-15T00:00 -0500"),
                price = 123.4,
                rooms = emptyList(),
                address = "123 Street, City, 1234",
                latitude = 0.0,
                longitude = 0.0,
            )
        )
    }
}