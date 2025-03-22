package com.combah.travel2.ui.lodgingsearch.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.R
import com.combah.travel2.common.ui.modifier.skeletonLoader
import com.combah.travel2.extensions.Time
import com.combah.travel2.ui.lodgingsearch.state.LodgingDetailsState
import com.combah.travel2.ui.lodgingsearch.state.LodgingRoomOfferState
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingDetails(state: LodgingDetailsState, onClose: () -> Unit, onAddToTripTapped: () -> Unit) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(state.name)
                    },
                    actions = {
                        IconButton(onClick = { onClose() }) {
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
                    AnimatedVisibility(visible = state.reviewCountText.isNotEmpty()) {
                        Text(state.reviewCountText)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(state.lodgingType)
                }
            }
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
        ) {
            Row(
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16F / 9F)
            ) {
                state.photos.firstOrNull()?.let {
                    LodgingImage(
                        it, modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                    )
                }
                AnimatedVisibility(state.photos.size >= 3) {
                    Column(
                        verticalArrangement = spacedBy(8.dp),
                    ) {
                        LodgingImage(
                            state.photos[1], modifier = Modifier
                                .weight(1F)
                                .matchWidthToHeight()
                        )
                        LodgingImage(
                            state.photos[2],
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.scrim.copy(
                                    alpha = 0.3F
                                ),
                                blendMode = BlendMode.SrcAtop,
                            ),
                            modifier = Modifier
                                .weight(1F)
                                .matchWidthToHeight()
                                .clickable { }
                        )
                    }
                }
            }
            Row(horizontalArrangement = spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = {/* Save lodging */ }) {
                    ButtonContent(
                        iconResId = R.drawable.bookmark_border_outline_24,
                        iconContentDescription = "Save button icon",
                        text = "Save lodging"
                    )
                }
                Button(
                    onClick = onAddToTripTapped,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.weight(1F)
                ) {
                    ButtonContent(
                        icon = Icons.Filled.Add,
                        iconContentDescription = "Add button icon",
                        text = "Add to trip"
                    )
                }
            }
            AnimatedContent(state.isLoading) {
                if (it) {
                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .skeletonLoader()
                    )
                } else {
                    var expandRooms by remember { mutableStateOf(false) }
                    val rooms = if (expandRooms) state.rooms else state.rooms.take(1)
                    rooms.forEach { room ->
                        Row(
                            horizontalArrangement = spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            room.photos.firstOrNull()?.let {
                                LodgingImage(
                                    it, modifier = Modifier
                                        .size(64.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1F)
                                    .align(Alignment.Top)
                            ) {
                                val features = listOf(
                                    "Breakfast Included" to room.breakfastIncluded,
                                    "Refundable" to room.refundable,
                                    "No pre-payment required" to !room.prePaymentRequired,
                                    "All inclusive" to room.isAllInclusive,
                                ).filter { it.second }.map { it.first }
                                Text(room.description, style = MaterialTheme.typography.labelLarge)
                                features.forEach { feature ->
                                    Text(
                                        text = AnnotatedString.Builder().apply {
                                            append("\u2022")
                                            append("\u0009")
                                            append(feature)
                                        }.toAnnotatedString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PriceText(room.price)
                                TextButton(onClick = {}) {
                                    ButtonContent(
                                        iconResId = R.drawable.open_in_new_outline_24,
                                        iconContentDescription = "Open offer button icon",
                                        text = room.bookingAgency
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp)
            ) {
                Image(Icons.Filled.Place, contentDescription = "Location icon")
                Text(state.address, style = MaterialTheme.typography.labelLarge)
            }
            AnimatedContent(state.isLoading) { isLoading ->
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(6 / 4f)
                            .clip(MaterialTheme.shapes.large)
                            .skeletonLoader(startDelayMillis = 300)
                    )
                } else {
                    val marker = LatLng(state.latitude, state.longitude)
                    val cameraPositionState =
                        rememberCameraPositionState(key = "${state.latitude},${state.longitude}") {
                            position = CameraPosition.fromLatLngZoom(marker, 15f)
                        }
                    GoogleMap(
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            indoorLevelPickerEnabled = false,
                            myLocationButtonEnabled = false,
                            scrollGesturesEnabled = false,
                            rotationGesturesEnabled = false,
                            tiltGesturesEnabled = false,
                            zoomGesturesEnabled = false,
                            zoomControlsEnabled = false,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(6 / 4f)
                            .clip(MaterialTheme.shapes.large)
                            .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Marker(
                            state = rememberMarkerState(position = marker),
                            title = state.name,
                            snippet = state.address
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonContent(
    @DrawableRes iconResId: Int? = null,
    icon: ImageVector? = null,
    iconContentDescription: String,
    text: String
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = iconContentDescription)
        } else if (iconResId != null) {
            Icon(
                painterResource(iconResId),
                contentDescription = iconContentDescription
            )
        }
        Text(text)
    }
}

@Composable
private fun LodgingImage(
    model: String,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null
) {
    val painter = rememberAsyncImagePainter(model = model, contentScale = ContentScale.Crop)
    Image(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        painter = painter,
        contentDescription = "Lodging Image Description",
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter,
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
    val initialState = LodgingDetailsState(
        name = "A very long Hotel name that might span multiple lines",
        rating = 4.5,
        reviewCountText = "123 reviews",
        lodgingType = "5-Star Hotel",
        photos = List(1) { index -> "" },
        checkIn = Time("2025-08-10T00:00 -0500"),
        checkOut = Time("2025-08-15T00:00 -0500"),
        price = 123.4,
        rooms = emptyList(),
        address = "123 Street, City, 1234",
        latitude = 0.0,
        longitude = 0.0,
        isLoading = true,
    )
    val loadedState = initialState.copy(
        photos = List(10) { index -> "" },
        rooms = List(7) { index ->
            LodgingRoomOfferState(
                photos = List(10) { "" },
                description = "Room $index",
                breakfastIncluded = index % 2 == 0,
                refundable = index % 2 != 0,
                prePaymentRequired = index % 2 == 0,
                isAllInclusive = index % 2 != 0,
                price = 123.4 * index,
                bookingUrl = "",
                bookingAgency = listOf("Expedia", "Booking", "Agoda")[index % 3],
            )
        },
        latitude = 37.56521,
        longitude = 126.98073,
        isLoading = false,
    )
    AppTheme {
        Box {
            var state by remember {
                mutableStateOf(initialState)
            }
            LodgingDetails(
                state = state,
                onClose = {},
                onAddToTripTapped = {},
            )
            if (state != loadedState) {
                Button(
                    onClick = { state = loadedState },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text("load")
                }
            } else {
                Button(
                    onClick = { state = initialState },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text("reset")
                }
            }
        }
    }
}