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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import travel.vola.android.common.ui.state.MarkerViewState

@Composable
fun MapScaffold(
    markers: List<MarkerViewState>,
    boundsPoints: List<LatLng>,
    minZoom: Float? = 15F,
    onMarkerTapped: (MarkerViewState) -> Unit = {},
    additionalContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isExpandedWindowSize =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val contentWidth = when {
        isExpandedWindowSize -> 400.dp
        else -> 320.dp
    }
    Row(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .then(
                    if (!isLargeScreen) {
                        Modifier.weight(1F)
                    } else {
                        Modifier.widthIn(max = contentWidth)
                    }
                )
                .fillMaxHeight()
        ) {
            content()
        }
        if (isLargeScreen) {
            Box(
                modifier = Modifier
                    .then(
                        if (isExpandedWindowSize) {
                            Modifier.widthIn(max = contentWidth)
                        } else {
                            Modifier.weight(1F)
                        }
                    )
                    .zIndex(1F)
            ) {
                additionalContent()
            }
            if (isExpandedWindowSize) {
                Map(markers, boundsPoints, onMarkerTapped, minZoom, modifier = Modifier.weight(1F))
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
            Marker(
                state = rememberMarkerState(key = position.toString(), position = position),
                title = markerState.name,
                icon = BitmapDescriptorFactory.fromBitmap(
                    mapMarkerIcon(
                        markerState.type,
                        selected = markerState.selected
                    )
                ),
                anchor = Offset(0.5F, 0.5F),
                onClick = {
                    onMarkerTapped(markerState)
                    false
                }
            )
        }
    }
}

@Preview(name = "2 - Portrait Tablet", device = "spec:parent=pixel_tablet,orientation=portrait")
@Preview(name = "3 - Landscape Tablet", device = "id:pixel_tablet")
annotation class TabletPreview

@Composable
@Preview
@TabletPreview
fun MapScaffoldPreview() {
    MapScaffold(markers = emptyList(), boundsPoints = listOf(LatLng(0.0, 0.0)), content = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    }, additionalContent = {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
        )
    })
}