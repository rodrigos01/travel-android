package travel.vola.android.ui.lodgingsearch.composable

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.R
import travel.vola.android.common.ui.components.ImageGallery
import travel.vola.android.common.ui.components.Overlay
import travel.vola.android.common.ui.components.OverlayHostProvider
import travel.vola.android.common.ui.components.asSizedImageTarget
import travel.vola.android.common.ui.components.rememberSizedImageState
import travel.vola.android.common.ui.modifier.skeletonLoader
import travel.vola.android.common.ui.preview.PreviewLightDarkSystemUI
import travel.vola.android.common.ui.preview.loremIpsum
import travel.vola.android.extensions.Time
import travel.vola.android.ui.lodgingsearch.state.LodgingDetailsState
import travel.vola.android.ui.lodgingsearch.state.LodgingReviewState
import travel.vola.android.ui.lodgingsearch.state.LodgingRoomOfferState
import travel.vola.android.ui.theme.AppTheme
import kotlin.math.roundToInt


@Composable
fun LodgingDetails(
    state: LodgingDetailsState,
    onClose: () -> Unit,
    onAddToTripTapped: () -> Unit,
    showMap: Boolean = true,
) {
    val contentState = rememberLodgingDetailsContentState()
    val coroutineScope = rememberCoroutineScope()
    OverlayHostProvider {
        Scaffold(
            topBar = {
                LodgingDetailsTopBar(state, contentState, coroutineScope, onClose)
            },
        ) { paddingValues ->
            LodgingDetailsContent(
                paddingValues,
                state,
                onAddToTripTapped,
                showMap,
                contentState,
                coroutineScope,
            )
        }
    }
}

class LodgingDetailsContentState(
    val scrollState: ScrollState,
) {
    private val _reviewsOffset = mutableStateOf<Offset?>(null)
    val reviewsOffset: State<Offset?> = _reviewsOffset
    internal fun setReviewsOffset(offset: Offset) {
        _reviewsOffset.value = offset
    }
}

@Composable
fun rememberLodgingDetailsContentState(): LodgingDetailsContentState {
    val scrollState = rememberScrollState()
    return remember { LodgingDetailsContentState(scrollState = scrollState) }
}

@Composable
fun LodgingDetailsContent(
    paddingValues: PaddingValues,
    state: LodgingDetailsState,
    onAddToTripTapped: () -> Unit = {},
    showMap: Boolean = true,
    contentState: LodgingDetailsContentState = rememberLodgingDetailsContentState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    var showExpandedMap by rememberSaveable(state) { mutableStateOf(false) }
    val marker = LatLng(state.latitude, state.longitude)
    var roomsOffset by remember { mutableStateOf<Offset?>(null) }
    var showImageGallery by rememberSaveable(state) { mutableStateOf(false) }
    var imageGalleryModels by rememberSaveable(state) { mutableStateOf(emptyList<String>()) }
    var selectedGalleryModel by rememberSaveable(state) { mutableStateOf<String?>(null) }
    fun onRoomCoverImageTapped(state: LodgingRoomOfferState, url: String) {
        imageGalleryModels = state.photos
        selectedGalleryModel = url
        showImageGallery = true
    }

    val context = LocalContext.current
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(contentState.scrollState)
            .padding(horizontal = 16.dp)
            .padding(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
            .animateContentSize(),
    ) {
        val photos = state.photos
        Row(
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16F / 9F)
        ) {
            photos.firstOrNull()?.let {
                val heroSizedState = rememberSizedImageState(it)
                LodgingImage(
                    rememberAsyncImagePainter(
                        model = heroSizedState.model,
                        contentScale = ContentScale.Crop
                    ),
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .clickable {
                            showImageGallery = true
                            imageGalleryModels = state.photos
                            selectedGalleryModel = it
                        }
                        .asSizedImageTarget(heroSizedState),
                )
            }
            AnimatedVisibility(photos.size >= 3) {
                Column(
                    verticalArrangement = spacedBy(8.dp),
                ) {
                    val image2SizedState = rememberSizedImageState(photos.getOrNull(1))
                    LodgingImage(
                        rememberAsyncImagePainter(
                            model = image2SizedState.model,
                            contentScale = ContentScale.Crop
                        ),
                        modifier = Modifier
                            .weight(1F)
                            .aspectRatio(1F)
                            .clickable {
                                showImageGallery = true
                                imageGalleryModels = state.photos
                                selectedGalleryModel = state.photos[1]
                            }
                            .asSizedImageTarget(image2SizedState),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .aspectRatio(1F)
                            .clickable {
                                showImageGallery = true
                                imageGalleryModels = state.photos
                                selectedGalleryModel = null
                            },
                    ) {
                        val sizedImageState = rememberSizedImageState(photos.getOrNull(2))
                        LodgingImage(
                            rememberAsyncImagePainter(
                                model = sizedImageState.model,
                                contentScale = ContentScale.Crop
                            ),
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.scrim.copy(
                                    alpha = 0.3F
                                ),
                                blendMode = BlendMode.SrcAtop,
                            ),
                            modifier = Modifier.asSizedImageTarget(sizedImageState)
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
            } else if (state.rooms.isNotEmpty()) {
                Column {
                    val room = state.rooms.first()
                    RoomOfferItem(room, onCoverImageTapped = { coverImage ->
                        onRoomCoverImageTapped(room, coverImage)
                    }, onViewOfferTapped = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(room.bookingUrl),
                            )
                        )
                    })
                    if (state.rooms.size > 1) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    contentState.scrollState.animateScrollTo(
                                        roomsOffset?.y?.roundToInt() ?: 0
                                    )
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                            Text("See ${state.rooms.size - 1} more offers")
                        }
                    }
                }
            }
        }
        state.description?.let { description ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            var hasMoreText by rememberSaveable { mutableStateOf(false) }
            Text(
                description,
                maxLines = if (expanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = {
                    if (it.didOverflowHeight) {
                        hasMoreText = true
                    }
                },
                modifier = Modifier.animateContentSize(),
            )
            if (hasMoreText) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) "Read less" else "Read more")
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
        if (showMap) {
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
                        val markerState = rememberUpdatedMarkerState(position = marker)
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
        if (state.reviews.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.onGloballyPositioned {
                    contentState.setReviewsOffset(it.positionInParent())
                },
            ) {
                Text("Reviews", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(state.reviewsUrl),
                        )
                    )
                }) {
                    ButtonContent(
                        iconResId = R.drawable.open_in_new_outline_24,
                        iconContentDescription = "Open reviews button icon",
                        text = "View all on ${state.reviewsSource}"
                    )
                }
            }
            Column(verticalArrangement = spacedBy(24.dp)) {
                state.reviews.forEach { review ->
                    LodgingReviewItem(review)
                }
            }
        }
        if (state.rooms.size > 1) {
            Text(
                "Rooms",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.onGloballyPositioned {
                    roomsOffset = it.positionInParent()
                })
            state.rooms.subList(1, state.rooms.size).forEach { room ->
                RoomOfferItem(room, onCoverImageTapped = { coverImage ->
                    onRoomCoverImageTapped(room, coverImage)
                }, onViewOfferTapped = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(room.bookingUrl),
                        )
                    )
                })
            }
        }
    }
    AnimatedVisibility(
        showImageGallery && imageGalleryModels.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.wrapContentSize(unbounded = true)
    ) {
        DismissableOverlay(onDismiss = {
            showImageGallery = false
        }) { paddingValues ->
            ImageGallery(
                imageGalleryModels,
                selectedInitially = selectedGalleryModel,
                contentPadding = paddingValues,
            )
        }
    }
    AnimatedVisibility(
        showExpandedMap, enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.wrapContentSize(unbounded = true)
    ) {
        DismissableOverlay(onDismiss = {
            showExpandedMap = false
        }) { paddingValues ->
            val cameraPositionState = rememberCameraPositionState()
            val markerState = rememberUpdatedMarkerState(position = marker)
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
                    .padding(paddingValues)
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
@OptIn(ExperimentalMaterial3Api::class)
fun LodgingDetailsTopBar(
    state: LodgingDetailsState,
    contentState: LodgingDetailsContentState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onClose: () -> Unit,
) {
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
            Text(
                state.reviewCount.reviewCountString(), modifier = Modifier
                    .clickable {
                        contentState.reviewsOffset.value?.y?.let {
                            coroutineScope.launch {
                                contentState.scrollState.animateScrollTo(it.roundToInt())
                            }
                        }
                    }
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(state.lodgingType)
        }
    }
}

@Composable
private fun DismissableOverlay(
    onDismiss: () -> Unit,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    Overlay {
        Box {
            val windowInsetsPadding = WindowInsets.safeDrawing.asPaddingValues()
            val layoutDirection = LocalLayoutDirection.current
            val density = LocalDensity.current
            var paddingValues by remember { mutableStateOf(windowInsetsPadding) }
            content(paddingValues)
            FilledIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .onGloballyPositioned {
                        paddingValues = PaddingValues(
                            top = windowInsetsPadding.calculateTopPadding() + with(density) { it.size.height.toDp() },
                            bottom = windowInsetsPadding.calculateBottomPadding(),
                            start = windowInsetsPadding.calculateStartPadding(layoutDirection),
                            end = windowInsetsPadding.calculateEndPadding(layoutDirection)
                        )
                    }
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = windowInsetsPadding.calculateTopPadding() + 8.dp)
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

@PreviewLightDarkSystemUI
@Composable
fun LodgingDetailsPreview() {
    val initialState = LodgingDetailsState(
        id = "lodgingId",
        name = "A very long Hotel name that might span multiple lines",
        rating = 4.5,
        reviewCount = 13450,
        lodgingType = "5-Star Hotel",
        photos = List(1) { index -> "" },
        checkIn = Time("2025-08-10T00:00 -0500"),
        checkOut = Time("2025-08-15T00:00 -0500"),
        price = 123.4,
        rooms = emptyList(),
        description = loremIpsum(),
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
        reviewsUrl = "",
        reviewsSource = "Tripadvisor",
        reviews = List(5) {
            LodgingReviewState(
                rating = 4.5,
                ratingImageUrl = "https://www.tripadvisor.com/img/cdsi/img2/ratings/traveler/s5.0-66827-5.svg",
                tripDate = Time("2023-08-15T00:00 GMT"),
                reviewTime = Time("2023-08-31T10:52 GMT"),
                authorAvatarUrl = "https://media-cdn.tripadvisor.com/media/photo-l/1a/f6/e4/2d/default-avatar-2020-48.jpg",
                authorName = "Author",
                authorLocation = "Author Location",
                review = loremIpsum(),
                title = "A Lovely stay"
            )
        },
        isLoading = false,
    )
    AppTheme {
        Box {
            LodgingDetails(
                state = loadedState,
                onClose = {},
                onAddToTripTapped = {},
            )
        }
    }
}
