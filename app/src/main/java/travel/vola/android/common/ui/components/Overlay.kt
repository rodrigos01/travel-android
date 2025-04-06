package travel.vola.android.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import travel.vola.android.common.ui.preview.PreviewLightDarkSystemUI
import travel.vola.android.ui.theme.AppTheme

@Composable
fun Overlay(content: @Composable () -> Unit) {
    var offsetX by remember { mutableIntStateOf(0) }
    Surface(
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7F),
        content = content,
        modifier = Modifier
            .wrapContentSize(unbounded = true)
            .onGloballyPositioned {
                offsetX = it.positionInWindow().x.toInt()
            }
            .offset {
                IntOffset(-offsetX, 0)
            }
            .width(LocalConfiguration.current.screenWidthDp.dp)
            .height(
                LocalConfiguration.current.screenHeightDp.dp
            )
    )
}

@Composable
@PreviewLightDarkSystemUI
fun OverlayPreview() {
    AppTheme {
        Overlay {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
                TextButton(
                    onClick = {}
                ) {
                    Text("Button")
                }
                Surface(
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9 / 16F)
                ) {}
            }
        }
    }
}

