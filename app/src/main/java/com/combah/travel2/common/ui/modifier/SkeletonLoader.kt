package com.combah.travel2.common.ui.modifier

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun Modifier.skeletonLoader(
    contentColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    startDelayMillis: Int = 0,
): Modifier {
    val transition = rememberInfiniteTransition()
    val animatedColor by transition.animateColor(
        initialValue = contentColor.copy(alpha = 0.2F),
        targetValue = contentColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(offsetMillis = startDelayMillis)
        )
    )
    return this
        .drawBehind {
            drawRect(animatedColor)
        }
}

@Composable
@Preview
fun SkeletonLoaderPreview() {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .skeletonLoader()
            )
        }
    }
}