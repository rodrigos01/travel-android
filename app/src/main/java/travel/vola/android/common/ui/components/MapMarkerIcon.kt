package travel.vola.android.common.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.ui.theme.AppTheme

private const val MARKER_SIZE = 24
private const val MARKER_SELECTED_SIZE = 32

@Composable
fun mapMarkerIcon(
    markerPainter: Painter,
    selected: Boolean = false,
    backgroundColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    },
    contentColor: Color = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    },
): Bitmap {
    val markerSizePx = (if (selected) MARKER_SELECTED_SIZE else MARKER_SIZE).dp.toPx()
    val paddingPx = 4.dp.toPx()
    val iconSizePx = markerSizePx - paddingPx * 2
    val bitmap = Bitmap.createBitmap(
        markerSizePx.toInt(), markerSizePx.toInt(), Bitmap.Config.ARGB_8888
    )
    val androidCanvas = android.graphics.Canvas(bitmap)
    CanvasDrawScope().draw(
        LocalDensity.current,
        LocalLayoutDirection.current,
        Canvas(androidCanvas),
        Size(markerSizePx, markerSizePx),
    ) {
        drawCircle(color = backgroundColor, radius = markerSizePx / 2F)
        translate(left = paddingPx, top = paddingPx) {
            markerPainter.apply {
                draw(
                    size = Size(iconSizePx, iconSizePx),
                    colorFilter = ColorFilter.tint(contentColor),
                )
            }
        }
    }
    return bitmap
}

@Composable
fun mapMarkerIcon(type: MarkerType, selected: Boolean = false): Bitmap {
    val icon = when (type) {
        MarkerType.Lodging -> painterResource(R.drawable.hotel_baseline_24)

        MarkerType.City -> painterResource(R.drawable.location_city_baseline_24)

        MarkerType.Place -> rememberVectorPainter(Icons.Filled.Place)
    }
    return mapMarkerIcon(icon, selected)
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
fun MapMarkerIconPreview() {
    AppTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Image(
                bitmap = mapMarkerIcon(MarkerType.Place, selected = true).asImageBitmap(),
                contentDescription = null
            )
        }
    }
}

