package travel.vola.android.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import travel.vola.android.common.ui.preview.TabletPreview
import travel.vola.android.common.ui.state.MarkerViewState

class MapScaffoldState internal constructor(
    val sizeClass: SizeClass,
    showMap: MutableState<Boolean>,
) {

    var showMap: Boolean by showMap
}

@Composable
fun rememberMapScaffoldState(
    mapInitiallyVisible: Boolean = false,
): MapScaffoldState {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return MapScaffoldState(
        sizeClass = when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> SizeClass.EXPANDED
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> SizeClass.MEDIUM
            else -> SizeClass.SMALL
        },
        remember { mutableStateOf(mapInitiallyVisible) },
    )
}

enum class SizeClass {
    SMALL, MEDIUM, EXPANDED
}

val SizeClass.isLargeScreen: Boolean
    get() = this != SizeClass.SMALL

@Composable
fun MapScaffold(
    markers: List<MarkerViewState>,
    boundsPoints: List<LatLng>,
    state: MapScaffoldState = rememberMapScaffoldState(),
    minZoom: Float? = 15F,
    onMarkerTapped: (MarkerViewState?) -> Unit = {},
    additionalContent: @Composable (PaddingValues) -> Unit = {},
    markerDescriptor: @Composable (MarkerViewState) -> BitmapDescriptor = {
        BitmapDescriptorFactory.fromBitmap(mapMarkerIcon(it.type, selected = it.selected))
    },
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    mapContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val isExpandedWindowSize = state.sizeClass == SizeClass.EXPANDED
    if (state.sizeClass.isLargeScreen) {
        val contentWidth = when {
            isExpandedWindowSize -> 400.dp
            else -> 320.dp
        }
        Row(Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .widthIn(max = contentWidth)
                    .fillMaxHeight(),
                topBar = topBar,
                bottomBar = bottomBar,
                content = content,
            )
            if (isExpandedWindowSize) {
                Box(
                    modifier = Modifier.widthIn(max = contentWidth)
                ) {
                    additionalContent(PaddingValues(0.dp))
                }
                Box {
                    Map(
                        markers,
                        boundsPoints,
                        onMarkerTapped,
                        minZoom,
                        markerDescriptor,
                    )
                    mapContent()
                }
            } else {
                Box {
                    Map(
                        markers,
                        boundsPoints,
                        onMarkerTapped,
                        minZoom,
                        markerDescriptor,
                    )
                    mapContent()
                    additionalContent(PaddingValues(0.dp))
                }
            }
        }
    } else {
        Scaffold(topBar = topBar, bottomBar = bottomBar, content = { paddingValues ->
            var additionalContentSize by remember { mutableStateOf(IntSize.Zero) }
            val showingContent = additionalContentSize == IntSize.Zero && !state.showMap
            if (showingContent) {
                content(paddingValues)
            }
            if (state.showMap) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 0.dp,
                            bottom = paddingValues.calculateBottomPadding(),
                            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                        )
                ) {
                    Map(
                        markers,
                        boundsPoints,
                        onMarkerTapped,
                        minZoom,
                        markerDescriptor,
                        modifier = Modifier
                    )
                    mapContent()
                }
            }
            Box(modifier = Modifier.onGloballyPositioned {
                additionalContentSize = it.size
            }) {
                additionalContent(paddingValues)
            }
        })
    }
}

@Composable
private fun Map(
    markers: List<MarkerViewState>,
    boundsPoints: List<LatLng>,
    onMarkerTapped: (MarkerViewState?) -> Unit,
    minZoom: Float?,
    markerDescriptor: @Composable (MarkerViewState) -> BitmapDescriptor,
    modifier: Modifier = Modifier,
) {
    val points = boundsPoints.takeIf { it.isNotEmpty() } ?: markers.map {
        LatLng(
            it.position.first, it.position.second
        )
    }
    val boundingBox =
        points.takeIf { it.isNotEmpty() }?.fold(LatLngBounds.Builder()) { builder, point ->
            builder.include(point)
        }?.build()
    val cameraPositionState = rememberCameraPositionState {
        position =
            boundingBox?.let { CameraPosition.fromLatLngZoom(boundingBox.center, 15F) }
                ?: CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 3F)
    }
    LaunchedEffect(boundingBox) {
        if (boundingBox != null) {
            val update = if (points.size > 1 || minZoom == null) {
                CameraUpdateFactory.newLatLngBounds(boundingBox, 64.dp.value.toInt())
            } else {
                CameraUpdateFactory.newLatLngZoom(boundingBox.center, minZoom)
            }
            cameraPositionState.animate(update)
        }
    }
    GoogleMap(
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            indoorLevelPickerEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
        ),
        contentPadding = WindowInsets.safeContent.asPaddingValues(),
        onMapClick = { onMarkerTapped(null) },
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        markers.forEach { markerState ->
            val position = LatLng(markerState.position.first, markerState.position.second)
            Marker(
                state = rememberUpdatedMarkerState(position = position),
                title = markerState.name,
                icon = markerDescriptor(markerState),
                anchor = Offset(0.5F, 0F),
                onClick = {
                    onMarkerTapped(markerState)
                    false
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MapScaffoldPreview(
    showMap: Boolean = false,
    hasAdditionContent: Boolean = false,
    showTopBar: Boolean = true,
) {
    MapScaffold(
        state = rememberMapScaffoldState(mapInitiallyVisible = showMap),
        markers = emptyList(),
        boundsPoints = listOf(LatLng(0.0, 0.0)),
        topBar = {
            if (showTopBar) {
                TopAppBar(title = { Text("Map Scaffold") })
            }
        },
        bottomBar = {
            TabBar(modifier = Modifier.fillMaxWidth()) {
                tab(
                    "search",
                    selected = true,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = null) })
                tab(
                    "map",
                    selected = false,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = null) })
            }
        },
        additionalContent = {
            if (hasAdditionContent) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .fillMaxSize()
                )
            }
        },
        mapContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .align(Alignment.BottomCenter),
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        },
    )
}

@Composable
@Preview(showSystemUi = true)
fun MapScaffoldPreviewMap() {
    MapScaffoldPreview(showMap = true, hasAdditionContent = false, showTopBar = false)
}

@Composable
@Preview
@TabletPreview
fun MapScaffoldPreviewMapTablet() {
    MapScaffoldPreview(showMap = true, hasAdditionContent = true)
}
