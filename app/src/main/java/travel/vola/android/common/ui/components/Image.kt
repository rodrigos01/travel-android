package travel.vola.android.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced

class SizedImageState(private val url: String?) {
    internal var width: Int by mutableIntStateOf(0)
    internal var height: Int by mutableIntStateOf(0)
    val model: String? by derivedStateOf {
        url?.replace("{width}", width.toString())?.replace("{height}", height.toString())
    }
}

@Composable
fun rememberSizedImageState(url: String?): SizedImageState =
    remember(url) { SizedImageState(url) }

fun Modifier.asSizedImageTarget(data: SizedImageState): Modifier {
    return this.onPlaced {
        data.width = it.size.width
        data.height = it.size.height
    }
}