package travel.vola.android.ui.lodgingsearch.composable

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import travel.vola.android.common.ui.components.toPx
import travel.vola.android.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun LodgingSearchMarkerIcon(text: String, selected: Boolean = false): Bitmap {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val textSize = with(LocalDensity.current) { 12.sp.toPx() }
    val textPaint = Paint().apply {
        color = contentColor.toArgb()
        this.textSize = textSize
    }
    val textBounds = Rect().also { textPaint.getTextBounds(text, 0, text.length, it) }
    val paddingX = 8.dp.toPx()
    val paddingY = 6.dp.toPx()
    val pinSizePx = 6.dp.toPx()
    val borderWidth = 2.dp.toPx()
    val width = textBounds.width() + (paddingX * 2)
    val height = textBounds.height() + (paddingY * 2)
    val bitmap = Bitmap.createBitmap(
        width.toInt() + borderWidth.toInt(),
        height.toInt() + pinSizePx.toInt() + borderWidth.toInt(),
        Bitmap.Config.ARGB_8888
    )
    val cornerRadius = 12.dp.toPx()
    Canvas(bitmap).apply {
        val fillPaint = Paint().apply {
            color = backgroundColor.toArgb()
        }
        val strokePaint = Paint().apply {
            color = MaterialTheme.colorScheme.outlineVariant.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            strokeCap = Paint.Cap.ROUND
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }
        drawRoundRect(
            borderWidth,
            borderWidth,
            width,
            height,
            cornerRadius,
            cornerRadius,
            fillPaint,
        )
        drawRoundRect(
            borderWidth,
            borderWidth,
            width,
            height,
            cornerRadius,
            cornerRadius,
            strokePaint,)
        val pinStartX = width / 2 - pinSizePx / 2
        val pinPath = Path().apply {
            moveTo(pinStartX, height)
            lineTo(pinStartX + pinSizePx, height)
            lineTo(width / 2, height + pinSizePx)
            lineTo(pinStartX, height)
        }
        drawPath(pinPath, fillPaint)
        drawPath(pinPath, strokePaint)
        drawText(text, paddingX, textBounds.height().toFloat() + paddingY - borderWidth / 2, textPaint)
    }
    return bitmap
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
fun LodgingSearchMarkerIconPreview() {
    AppTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Image(
                bitmap = LodgingSearchMarkerIcon("$200").asImageBitmap(),
                contentDescription = null
            )
        }
    }
}