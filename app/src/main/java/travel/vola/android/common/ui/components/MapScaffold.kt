package travel.vola.android.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
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
import com.google.maps.android.compose.rememberMarkerState
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
    showMap: MutableState<Boolean> = mutableStateOf(false),
): MapScaffoldState {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return MapScaffoldState(
        sizeClass = when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> SizeClass.EXPANDED
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> SizeClass.MEDIUM
            else -> SizeClass.SMALL
        },
        showMap,
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
    onMarkerTapped: (MarkerViewState) -> Unit = {},
    additionalContent: @Composable () -> Unit = {},
    markerDescriptor: @Composable (MarkerViewState) -> BitmapDescriptor = {
        BitmapDescriptorFactory.fromBitmap(mapMarkerIcon(it.type, selected = it.selected))
    },
    content: @Composable () -> Unit,
) {
    val isExpandedWindowSize = state.sizeClass == SizeClass.EXPANDED
    if (state.sizeClass.isLargeScreen) {
        val contentWidth = when {
            isExpandedWindowSize -> 400.dp
            else -> 320.dp
        }
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .widthIn(max = contentWidth)
                    .fillMaxHeight()
            ) {
                content()
            }
            if (isExpandedWindowSize) {
                Box(
                    modifier = Modifier.widthIn(max = contentWidth)
                ) {
                    additionalContent()
                }
                Map(
                    markers,
                    boundsPoints,
                    onMarkerTapped,
                    minZoom,
                    markerDescriptor,
                )
            } else {
                Box {
                    Map(
                        markers,
                        boundsPoints,
                        onMarkerTapped,
                        minZoom,
                        markerDescriptor,
                    )
                    additionalContent()
                }
            }
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            content()
            additionalContent()
            if (state.showMap) {
                Map(
                    markers,
                    boundsPoints,
                    onMarkerTapped,
                    minZoom,
                    markerDescriptor,
                )
            }
        }
    }
}

@Composable
private fun Map(
    markers: List<MarkerViewState>,
    boundsPoints: List<LatLng>,
    onMarkerTapped: (MarkerViewState) -> Unit,
    minZoom: Float?,
    markerDescriptor: @Composable (MarkerViewState) -> BitmapDescriptor,
    modifier: Modifier = Modifier,
) {
    if (boundsPoints.isEmpty()) return
    val boundingBox = boundsPoints.fold(LatLngBounds.Builder()) { builder, point ->
        builder.include(point)
    }.build()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(boundingBox.center, 15F)
    }
    LaunchedEffect(boundingBox) {
        val update = if (boundsPoints.size > 1 || minZoom == null) {
            CameraUpdateFactory.newLatLngBounds(boundingBox, 64.dp.value.toInt())
        } else {
            CameraUpdateFactory.newLatLngZoom(boundingBox.center, minZoom)
        }
        cameraPositionState.animate(update)
    }
    GoogleMap(
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            indoorLevelPickerEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
        ),
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        markers.forEach { markerState ->
            val position = LatLng(markerState.position.first, markerState.position.second)
            Marker(state = rememberMarkerState(key = position.toString(), position = position),
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

@Composable
@Preview
@TabletPreview
fun MapScaffoldPreview() {
    MapScaffold(state = rememberMapScaffoldState(showMap = remember { mutableStateOf(false) }),
        markers = emptyList(),
        boundsPoints = listOf(LatLng(0.0, 0.0)),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        },
        additionalContent = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxSize()
            )
        })
}