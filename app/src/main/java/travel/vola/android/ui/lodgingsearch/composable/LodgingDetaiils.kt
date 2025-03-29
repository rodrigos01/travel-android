package travel.vola.android.ui.lodgingsearch.composable

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.vola.android.R
import com.vola.android.common.ui.components.ImageGallery
import com.vola.android.common.ui.components.Overlay
import com.vola.android.common.ui.modifier.matchWidthToHeight
import com.vola.android.common.ui.modifier.skeletonLoader
import com.vola.android.common.ui.preview.PreviewLightDarkSystemUI
import com.vola.android.extensions.Time
import com.vola.android.ui.lodgingsearch.state.LodgingDetailsState
import com.vola.android.ui.lodgingsearch.state.LodgingRoomOfferState
import com.vola.android.ui.theme.AppTheme
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingDetails(state: LodgingDetailsState, onClose: () -> Unit, onAddToTripTapped: () -> Unit) {
    var showImageGallery by remember(state) { mutableStateOf(false) }
    var imageGalleryModels by remember(state) { mutableStateOf(emptyList<String>()) }
    var selectedGalleryModel by remember(state) { mutableStateOf<String?>(null) }
    var showExpandedMap by remember(state) { mutableStateOf(false) }
    val marker = LatLng(state.latitude, state.longitude)
    Scaffold(topBar = {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
            TopAppBar(title = {
                Text(state.name)
            }, actions = {
                IconButton(onClick = { onClose() }) {
                    Icon(Icons.Filled.Close, contentDescription = "")
                }
            })
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
    }) { paddingValues ->
        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                )
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
                        rememberAsyncImagePainter(model = it, contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .clickable {
                                showImageGallery = true
                                imageGalleryModels = state.photos
                                selectedGalleryModel = it
                            },
                    )
                }
                AnimatedVisibility(state.photos.size >= 3) {
                    Column(
                        verticalArrangement = spacedBy(8.dp),
                    ) {
                        LodgingImage(
                            rememberAsyncImagePainter(
                                model = state.photos[1],
                                contentScale = ContentScale.Crop
                            ),
                            modifier = Modifier
                                .weight(1F)
                                .matchWidthToHeight().clickable {
                                    showImageGallery = true
                                    imageGalleryModels = state.photos
                                    selectedGalleryModel = state.photos[1]
                                },
                        )
                        Box(
                            modifier = Modifier
                                .weight(1F)
                                .matchWidthToHeight()
                                .clickable {
                                    showImageGallery = true
                                    imageGalleryModels = state.photos
                                },
                        ) {
                            LodgingImage(
                                rememberAsyncImagePainter(
                                    model = state.photos[2],
                                    contentScale = ContentScale.Crop
                                ),
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.scrim.copy(
                                        alpha = 0.3F
                                    ),
                                    blendMode = BlendMode.SrcAtop,
                                ),
                            )
                            Text(
                                text = "+${state.photos.size - 2}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
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
                    Column(modifier = Modifier.animateContentSize()) {
                        var expandRooms by remember { mutableStateOf(false) }
                        val rooms = if (expandRooms) state.rooms else state.rooms.take(1)
                        rooms.forEach { room ->
                            Row(
                                horizontalArrangement = spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val roomCoverPhoto = room.photos.firstOrNull()
                                if (roomCoverPhoto != null) {
                                    LodgingImage(
                                        rememberAsyncImagePainter(
                                            model = roomCoverPhoto,
                                            contentScale = ContentScale.Crop
                                        ),
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clickable {
                                                imageGalleryModels = room.photos
                                                selectedGalleryModel = roomCoverPhoto
                                                showImageGallery = true
                                            },
                                    )
                                } else {
                                    LodgingImage(
                                        painterResource(R.drawable.ic_hotel_black_24dp),
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
                                        "Breakfast Included" to room.breakfastIncluded,
                                        "Refundable" to room.refundable,
                                        "No pre-payment required" to !room.prePaymentRequired,
                                        "All inclusive" to room.isAllInclusive,
                                    ).filter { it.second }.map { it.first }
                                    Text(
                                        room.description,
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
                                            }.toAnnotatedString(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(112.dp)
                                ) {
                                    PriceText(room.price)
                                    val context = LocalContext.current
                                    TextButton(onClick = {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(room.bookingUrl),
                                            )
                                        )
                                    }) {
                                        ButtonContent(
                                            iconResId = R.drawable.open_in_new_outline_24,
                                            iconContentDescription = "Open offer button icon",
                                            text = room.bookingAgency
                                        )
                                    }
                                }
                            }
                        }
                        TextButton(
                            onClick = { expandRooms = !expandRooms },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                if (expandRooms) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                            Text(if (expandRooms) "See less offers" else "Show ${state.rooms.size} more offers")
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(6 / 4f)
                            .clip(MaterialTheme.shapes.large)
                            .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        val cameraPositionState = rememberCameraPositionState()
                        val markerState = rememberMarkerState(position = marker)
                        LaunchedEffect(marker) {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(marker, 15f)
                            markerState.position = marker
                        }
                        GoogleMap(
                            cameraPositionState = cameraPositionState,
                            googleMapOptionsFactory = {
                                GoogleMapOptions().liteMode(true)
                            },
                            uiSettings = MapUiSettings(
                                indoorLevelPickerEnabled = false,
                                myLocationButtonEnabled = false,
                                scrollGesturesEnabled = false,
                                rotationGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                zoomControlsEnabled = false,
                                mapToolbarEnabled = false,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Marker(
                                state = markerState, title = state.name, snippet = state.address
                            )
                        }
                        Surface(color = Color.Transparent, onClick = {
                            showExpandedMap = true
                        }, modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
    AnimatedVisibility(
        showImageGallery && imageGalleryModels.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        DismissableOverlay(onDismiss = {
            showImageGallery = false
        }) { topPadding ->
            ImageGallery(
                imageGalleryModels,
                selectedInitially = selectedGalleryModel,
                modifier = Modifier
                    .padding(top = topPadding + 8.dp),
            )
        }
    }
    AnimatedVisibility(
        showExpandedMap, enter = fadeIn(),
        exit = fadeOut(),
    ) {
        DismissableOverlay(onDismiss = {
            showExpandedMap = false
        }) { topPadding ->
            val cameraPositionState = rememberCameraPositionState()
            val markerState = rememberMarkerState(position = marker)
            LaunchedEffect(marker) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(marker, 15f)
                markerState.position = marker
            }
            GoogleMap(
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    indoorLevelPickerEnabled = false,
                    myLocationButtonEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = topPadding + 8.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8F)
                    .clip(MaterialTheme.shapes.large)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Marker(
                    state = markerState, title = state.name, snippet = state.address
                )
            }
        }
    }
}

@Composable
private fun DismissableOverlay(
    onDismiss: () -> Unit,
    content: @Composable (topPadding: Dp) -> Unit
) {
    Overlay {
        Box {
            var topPadding by remember { mutableIntStateOf(0) }
            content(with(LocalDensity.current) { topPadding.toDp() })
            Column(
                modifier = Modifier
                    .onGloballyPositioned {
                        topPadding = it.size.height + it.positionInParent().y.roundToInt()
                    }
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                FilledIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
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
                painterResource(iconResId), contentDescription = iconContentDescription
            )
        }
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LodgingImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    colorFilter: ColorFilter? = null
) {
    Image(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        painter = painter,
        contentDescription = "Lodging Image Description",
        contentScale = contentScale,
        colorFilter = colorFilter,
    )
}

@PreviewLightDarkSystemUI
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
        photos = List(54) { index -> "photo$index" },
        rooms = List(7) { index ->
            LodgingRoomOfferState(
                photos = if (index % 2 == 0) List(35) { "roomPhoto$it" } else emptyList(),
                description = "Room $index",
                breakfastIncluded = index % 2 == 0,
                refundable = index % 2 != 0,
                prePaymentRequired = index % 2 == 0,
                isAllInclusive = index % 2 != 0,
                price = 123.4 * index,
                bookingUrl = "",
                bookingAgency = listOf("Expedia", "Booking", "Agoda", "ZenHotels.com")[index % 4],
            )
        },
        latitude = 37.56521,
        longitude = 126.98073,
        isLoading = false,
    )
    AppTheme {
        Box {
            var state by remember {
                mutableStateOf(loadedState)
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
