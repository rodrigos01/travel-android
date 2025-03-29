package travel.vola.android.common.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import travel.vola.android.common.ui.components.TabbedHostScope.Tab
import travel.vola.android.ui.theme.AppTheme
import kotlin.math.max
import kotlin.math.roundToInt

class TabbedHostScope(
    builder: TabbedGraphBuilder.() -> Unit,
    val navigate: (String) -> Unit
) {
    class Tab(
        val id: String,
        val icon: @Composable () -> Unit = {},
        val title: @Composable () -> Unit = {},
        val modifier: Modifier = Modifier,
        val content: @Composable TabbedHostScope.() -> Unit,
    )

    val tabs =
        mutableListOf<Tab>().apply { TabbedGraphBuilder().apply(builder).opps.forEach { it() } }

    fun findTab(tabId: String) =
        tabs.firstOrNull { it.id == tabId } ?: Tab(
            tabId,
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null
                )
            },
            content = { Text("No Tab Found for id $tabId") }
        )
}

class TabbedGraphBuilder {
    val opps = mutableListOf<(MutableList<Tab>.() -> Unit)>()
    fun tab(
        tabId: String,
        icon: @Composable () -> Unit = {},
        title: @Composable () -> Unit = {},
        modifier: Modifier = Modifier,
        content: @Composable TabbedHostScope.() -> Unit,
    ) {
        opps.add { add(Tab(tabId, icon, title, modifier, content)) }
    }
}

@Composable
fun TabbedHost(
    startDestination: String,
    tabBarListState: LazyListState = rememberLazyListState(),
    builder: TabbedGraphBuilder.() -> Unit
) {
    var currentTabId by remember { mutableStateOf(startDestination) }
    val scope = TabbedHostScope(builder, navigate = { currentTabId = it })
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
        ) {
            scope.findTab(currentTabId).content(scope)
        }
        AnimatedVisibility(
            visible = scope.tabs.size > 1, modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
//            LaunchedEffect(currentTabId) {
//                tabBarListState.animateScrollToItem(scope.tabs.indexOfFirst { it.id == currentTabId })
//            }
            LazyRow(
                state = tabBarListState,
            ) {
                items(scope.tabs, key = { it.id }) { tab ->
                    val selected = tab.id == currentTabId
                    val tabSize = with(LocalDensity.current) { 48.dp.toPx() }
                    var targetOffsetX by remember { mutableFloatStateOf(-tabSize) }
                    var zIndex by remember { mutableFloatStateOf(-1F) }
                    val offsetX by animateFloatAsState(
                        targetOffsetX,
                        finishedListener = { zIndex = 0F },
                    )
                    FilledTonalButton(
                        onClick = { scope.navigate(tab.id) },
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
    }
}

@Preview
@Composable
fun TabbedHostPreview() {
    AppTheme {
        var openedTabs by remember { mutableStateOf(listOf("tab_0" to "Tab O")) }
        var selectedTabId by remember { mutableStateOf("home") }
        TabbedHost(startDestination = selectedTabId) {
            tab(
                "home",
                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                title = { Text("Home") },
            ) {
                Column {
                    Button(onClick = {
                        val index = openedTabs.size
                        val id = "tab_$index"
                        openedTabs += id to "Tab $index"
                        navigate(id)
                        GlobalScope.launch {
                            delay(1000)
                            openedTabs = openedTabs.map {
                                if (it.first == id) {
                                    id to "Tab $index (Updated)"
                                } else {
                                    it
                                }
                            }
                        }
                    }) {
                        Text("Open New Tab")
                    }
                }
            }
            openedTabs.forEach {
                tab(
                    it.first,
                    icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    title = { Text(it.second) },
                ) {
                    Column {
                        Button(onClick = {
                            val index = openedTabs.indexOf(it)
                            navigate(openedTabs[max(0, index - 1)].first)
                            openedTabs -= it
                        }) {
                            Text("Close Tab")
                        }
                        Text(it.second)
                    }
                }
            }
        }
    }
}
