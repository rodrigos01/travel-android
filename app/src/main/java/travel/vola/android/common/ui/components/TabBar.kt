package travel.vola.android.common.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

object TabBar {
    class Tab(
        val id: String,
        val selected: Boolean = false,
        val icon: @Composable () -> Unit = {},
        val title: @Composable () -> Unit = {},
        val modifier: Modifier = Modifier,
    )

    class Builder {
        private val tabs = mutableListOf<Tab>()

        fun tab(
            id: String,
            selected: Boolean = false,
            icon: @Composable () -> Unit = {},
            title: @Composable () -> Unit = {},
            modifier: Modifier = Modifier,
        ) = tabs.add(Tab(id, selected, icon, title, modifier))

        fun build(): List<Tab> = tabs
    }
}

@Composable
fun TabBar(
    modifier: Modifier = Modifier,
    tabBarListState: LazyListState = rememberLazyListState(),
    onTabClick: (String) -> Unit = {},
    builder: TabBar.Builder.() -> Unit,
) {
    val tabs = TabBar.Builder().apply(builder).build()
    LazyRow(
        state = tabBarListState,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer
            )
            .then(modifier),
    ) {
        items(tabs, key = { it.id }) { tab ->
            val selected = tab.selected
            val tabSize = with(LocalDensity.current) { 48.dp.toPx() }
            var targetOffsetX by remember { mutableFloatStateOf(-tabSize) }
            var zIndex by remember { mutableFloatStateOf(-1F) }
            val offsetX by animateFloatAsState(
                targetOffsetX,
                finishedListener = { zIndex = 0F },
            )
            FilledTonalButton(
                onClick = { onTabClick(tab.id) },
                colors = if (selected) {
                    ButtonDefaults.filledTonalButtonColors()
                } else {
                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                },
                shape = MaterialTheme.shapes.medium,
                modifier = tab.modifier
                    .onPlaced {
                        targetOffsetX = 0F
                    }
                    .offset {
                        IntOffset(x = offsetX.roundToInt(), y = 0)
                    }
                    .zIndex(zIndex)
                    .animateItem()
            ) {
                Row(
                    horizontalArrangement = spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tab.icon()
                    tab.title()
                }
            }
        }
    }
}

@Composable
@Preview
fun TabBarPreview() {
    TabBar {
        tab(
            "home",
            selected = true,
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            title = { Text("Home") },
        )
        tab(
            "search",
            icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            title = { Text("Search") },
        )
    }
}