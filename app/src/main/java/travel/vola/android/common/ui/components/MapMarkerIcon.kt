package travel.vola.android.common.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.DrawableCompat
import travel.vola.android.R
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.ui.theme.AppTheme

private const val MARKER_SIZE = 24

@Composable
fun mapMarkerIcon(
    type: MarkerType,
): Bitmap {
    val markerSizePx = MARKER_SIZE.dp.toPx().toInt()
    val paddingPx = 4.dp.toPx().toInt()
    val iconSizePx = markerSizePx - paddingPx
    val bitmap = Bitmap.createBitmap(
        markerSizePx,
        markerSizePx,
        Bitmap.Config.ARGB_8888
    )
    val icon = when (type) {
        MarkerType.Lodging -> R.drawable.ic_hotel_black_24dp
        MarkerType.City -> R.drawable.baseline_location_city_24
    }.let { resId ->
        AppCompatResources.getDrawable(LocalContext.current, resId)
    }?.also {
        it.setBounds(paddingPx, paddingPx, iconSizePx, iconSizePx)
        DrawableCompat.setTint(it, MaterialTheme.colorScheme.onTertiaryContainer.toArgb())
    }
    Canvas(bitmap).apply {
        drawCircle(markerSizePx / 2F, markerSizePx / 2F, markerSizePx / 2F, Paint().apply {
            color = MaterialTheme.colorScheme.tertiaryContainer.toArgb()
        })
        icon?.draw(this@apply)
    }
    return bitmap
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
                bitmap = mapMarkerIcon(MarkerType.Lodging).asImageBitmap(),
                contentDescription = null
            )
        }
    }
}

