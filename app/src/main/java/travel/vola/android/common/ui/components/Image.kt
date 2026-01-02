package travel.vola.android.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import travel.vola.android.R

class SizedImageState(private val url: String?) {
    internal var width: Int by mutableIntStateOf(0)
    internal var height: Int by mutableIntStateOf(0)
    val model: String? by derivedStateOf {
        url?.replace("{width}", width.toString())?.replace("{height}", height.toString())
    }

    fun updateSize(size: IntSize) {
        width = size.width
        height = size.height
    }
}

@Composable
fun rememberSizedImageState(url: String?): SizedImageState = remember(url) { SizedImageState(url) }

fun Modifier.asSizedImageTarget(data: SizedImageState): Modifier {
    return this.onSizeChanged(data::updateSize)
}

@Composable
fun placeholderPainter(): Painter {
    val images = listOf(
        R.drawable.background_alps,
        R.drawable.background_bistro,
        R.drawable.background_eiffel,
        R.drawable.background_kyoto,
        R.drawable.background_nordeste,
        R.drawable.background_nyc,
        R.drawable.background_old_town,
        R.drawable.background_pub,
    )
    return if (LocalInspectionMode.current) {
        painterResource(images.random())
    } else {
        ColorPainter(Color.Transparent)
    }
}