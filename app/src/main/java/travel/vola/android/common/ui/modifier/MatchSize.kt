package travel.vola.android.common.ui.modifier

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity

@Composable
private fun Int.toDp() = with(LocalDensity.current) { toDp() }

@Composable
fun Modifier.matchWidthToHeight(): Modifier {
    var width by remember { mutableIntStateOf(0) }
    return this
        .width(width.toDp())
        .onPlaced {
            width = it.size.height
        }
}

@Composable
fun Modifier.matchHeightToWidth(): Modifier {
    var height by remember { mutableIntStateOf(0) }
    return this
        .height(height.toDp())
        .onPlaced {
            height = it.size.width
        }
}
