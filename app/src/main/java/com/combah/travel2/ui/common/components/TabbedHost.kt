package com.combah.travel2.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.combah.travel2.ui.common.components.TabbedHostScope.Tab
import com.combah.travel2.ui.theme.AppTheme
import kotlin.math.max
import kotlin.math.roundToInt

class TabbedHostScope(startDestination: String, builder: TabbedGraphBuilder) {
    class Tab(
        val id: String,
        val icon: @Composable () -> Unit = {},
        val title: @Composable () -> Unit = {},
        val content: @Composable TabbedHostScope.() -> Unit,
    )

    val tabs = mutableStateListOf(*builder.tabs.toTypedArray())
    var currentTab by mutableStateOf(findTab(startDestination))
        private set

    fun openTab(
        tabId: String,
        icon: @Composable () -> Unit = {},
        title: @Composable () -> Unit = {},
        content: @Composable TabbedHostScope.() -> Unit,
    ) {
        tabs.add(Tab(tabId, icon, title, content))
        navigate(tabId)
    }

    fun closeTab(tabId: String) {
        val index = tabs.indexOfFirst { it.id == tabId }
        navigate(tabs[max(0, index - 1)].id)
        tabs.removeAt(index)
    }

    fun navigate(tabId: String) {
        currentTab = findTab(tabId)
    }

    fun findTab(tabId: String) =
        tabs.find { it.id == tabId } ?: Tab(
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
    val tabs = mutableListOf<Tab>()
    fun tab(
        tabId: String,
        icon: @Composable () -> Unit = {},
        title: @Composable () -> Unit = {},
        content: @Composable TabbedHostScope.() -> Unit,
    ) {
        tabs.add(Tab(tabId, icon, title, content))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedHost(
    startDestination: String,
    builder: TabbedGraphBuilder.() -> Unit
) {
    val scope = remember { TabbedHostScope(startDestination, TabbedGraphBuilder().apply(builder)) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        scope.currentTab.content(scope)
        AnimatedVisibility(
            visible = scope.tabs.size > 1, modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            val tabBarState = rememberLazyListState()
            LaunchedEffect(scope.currentTab) {
                tabBarState.animateScrollToItem(scope.tabs.indexOf(scope.currentTab))
            }
            LazyRow(
                state = tabBarState,
                modifier = Modifier
            ) {
                items(scope.tabs, key = { it.id }) { tab ->
                    val selected = tab == scope.currentTab
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
                        modifier = Modifier
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
        TabbedHost(startDestination = "home") {
            tab(
                "home",
                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                title = { Text("Home") },
            ) {
                var openedTabs by rememberSaveable { mutableIntStateOf(0) }
                val onClick = remember(openedTabs) {
                    {
                        openTab(
                            "email_$openedTabs",
                            icon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                            title = { Text("Email") },) {
                            Button(onClick = { closeTab("email") }) {
                                Text("Close Tab")
                            }
                        }
                    }
                }
                Button(onClick = {
                    onClick()
                    openedTabs++
                }) {
                    Text("Open New Tab")
                }
            }
            tab(
                "search",
                icon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                title = { Text("Search") }) {
                Text("Search")
            }
            tab(
                "Other",
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                title = { Text("A Long Title Info") }) {
                Text("Search")
            }
        }
    }
}
