package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.R
import com.combah.travel2.common.ui.components.TabbedHost
import com.combah.travel2.common.ui.components.TabbedHostScope
import com.combah.travel2.common.ui.modifier.skeletonLoader
import com.combah.travel2.extensions.Time
import com.combah.travel2.ui.lodgingsearch.state.LodgingDetailsState
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import com.combah.travel2.ui.lodgingsearch.viewmodel.LodgingSearchViewModel
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.NumberFormat

@Composable
private fun LodgingSearch(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    onLodgingTapped: (LodgingSearchResultState) -> Unit,
    onLodgingClosed: (String) -> Unit,
    onAddLodgingTapped: (String) -> Unit,
    onContinueBrowsingTapped: () -> Unit,
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit,
    onFiltersApplied: (minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>) -> Unit,
) {
    if (state.localState.showAddConfirmation) {
        ConfirmationDialog(
            onConfirm = { navController.popBackStack() },
            onDismiss = onContinueBrowsingTapped,
            confirmButtonLabel = "Back to Trip",
            dismissButtonLabel = "Continue browsing"
        ) {
            Text("Added to your trip. Do you want to continue browsing?")
        }
    }
    val searchTabListState = rememberLazyListState()
    val tabBarListState = rememberLazyListState()
    var openedResultId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(openedResultId) {
        if (openedResultId != null && state is LodgingSearchViewModel.UiState.Loaded) {
            tabBarListState.animateScrollToItem(state.openedResults.keys.indexOf(openedResultId))
        }
    }
    val searchResults: @Composable TabbedHostScope.() -> Unit = {
        LodgingSearchResults(
            navController,
            state,
            scrollState = searchTabListState,
            onLodgingTapped = { lodging ->
                onLodgingTapped(lodging)
                navigate(lodging.id)
                openedResultId = lodging.id
            },
            onSortOptionSelected = onSortOptionSelected,
            onFiltersApplied = onFiltersApplied,
        )
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    TabbedHost(startDestination = "search", tabBarListState = tabBarListState) {
        tab("search", icon = { Icon(Icons.Outlined.Search, contentDescription = null) }) {
            searchResults()
        }
        if (state is LodgingSearchViewModel.UiState.Loaded) {
            state.openedResults.forEach { (id, lodging) ->
                tab(
                    id,
                    title = {
                        Text(
                            text = lodging.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.widthIn(max = (screenWidth / 2).dp),
                    content = {
                        LodgingDetails(lodging, onClose = {
                            onLodgingClosed(id)
                            navigate("search")
                        }, onAddToTripTapped = {
                            onAddLodgingTapped(id)
                        })
                    })
            }
        }
    }
}

enum class ControlsVisible {
    NONE,
    FILTERS,
    SORT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingSearchResults(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    scrollState: LazyListState = rememberLazyListState(),
    onLodgingTapped: (LodgingSearchResultState) -> Unit = {},
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit = {},
    onFiltersApplied: (minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>) -> Unit = { _, _, _ -> },
) {
    val coroutineScope = rememberCoroutineScope()
    var controlsVisible by remember { mutableStateOf(ControlsVisible.NONE) }
    Scaffold(topBar = {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(bottom = 8.dp)
                .animateContentSize()
        ) {
            TopAppBar(title = { Text("Lodging Search") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            })
            LodgingSearchParams(
                checkIn = state.searchState.checkIn,
                checkOut = state.searchState.checkOut,
                minCheckOut = state.searchState.minCheckOut,
                locationText = state.searchState.locationText,
                onCheckInDateSelected = {},
                onCheckOutDateSelected = {},
                onLocationSearchResultSelected = {},
                onLocationSearchTextChanged = {},
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextButton(onClick = {
                    controlsVisible = ControlsVisible.FILTERS
                }) {
                    Icon(
                        painterResource(R.drawable.tune_baseline_24),
                        contentDescription = null
                    )
                    Text("Filter")
                }
                TextButton(onClick = {
                    controlsVisible = ControlsVisible.SORT
                }) {
                    Icon(
                        painterResource(R.drawable.sort_baseline_24),
                        contentDescription = null
                    )
                    Text("Sort")
                }
            }
            AnimatedContent(
                targetState = controlsVisible,
                transitionSpec = {
                    val direction = if (initialState == ControlsVisible.NONE) {
                        AnimatedContentTransitionScope.SlideDirection.Down
                    } else {
                        when (targetState) {
                            ControlsVisible.SORT -> AnimatedContentTransitionScope.SlideDirection.Start
                            ControlsVisible.FILTERS -> AnimatedContentTransitionScope.SlideDirection.End
                            ControlsVisible.NONE -> AnimatedContentTransitionScope.SlideDirection.Up
                        }
                    }
                    val slideSpec = spring<IntOffset>(stiffness = Spring.StiffnessMediumLow)
                    (fadeIn() + slideIntoContainer(direction, slideSpec)).togetherWith(
                        slideOutOfContainer(direction, slideSpec) + fadeOut()
                    )
                },
            ) { currentControls ->
                when (currentControls) {
                    ControlsVisible.FILTERS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val filterState = FilterOptionsState(
                                minRating = state.sortAndFilterState.minRating,
                                minStars = state.sortAndFilterState.minStars,
                                priceRange = state.sortAndFilterState.priceRange
                            )
                            FilterOptions(
                                state = filterState,
                                valueRange = state.sortAndFilterState.availablePriceRange,
                            )
                            Row(modifier = Modifier.align(Alignment.End)) {
                                TextButton(onClick = {
                                    controlsVisible = ControlsVisible.NONE
                                }) {
                                    Text(
                                        "Cancel",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = {
                                    controlsVisible = ControlsVisible.NONE
                                    onFiltersApplied(
                                        filterState.minRating.toDouble(),
                                        filterState.minStars,
                                        filterState.priceRange.start.toDouble()..filterState.priceRange.endInclusive.toDouble(),
                                    )
                                }) {
                                    Text("Apply")
                                }
                            }
                        }
                    }

                    ControlsVisible.SORT -> SortOptionSelector(
                        state.sortAndFilterState,
                        onSortOptionSelected = { option ->
                            onSortOptionSelected(option)
                            controlsVisible = ControlsVisible.NONE
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        })

                    ControlsVisible.NONE -> {}
                }
            }
        }
    }) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            when (state) {
                is LodgingSearchViewModel.UiState.Loading -> {
                    loading()
                }

                is LodgingSearchViewModel.UiState.Loaded -> {
                    loaded(state, onLodgingTapped)
                }
            }
        }
    }
}

private fun LazyListScope.loaded(
    state: LodgingSearchViewModel.UiState.Loaded,
    onLodgingTapped: (LodgingSearchResultState) -> Unit
) {
    items(state.results, key = { it.id }) { result ->
        Surface(shape = MaterialTheme.shapes.large,
            border = BorderStroke(
                1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .animateItem(),
            onClick = { onLodgingTapped(result) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(model = result.coverImage),
                    contentDescription = "Place Description",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.77f)
                        .background(color = MaterialTheme.colorScheme.tertiary),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(result.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        result.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 16.dp),
                    ) {
                        LodgingRating(result.rating)
                        Text(
                            result.lodgingType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        PriceText(result.price)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.loading() {
    items(3) { index ->
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .skeletonLoader(startDelayMillis = index * 300)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
            )
            Box(Modifier.height(64.dp))
        }
    }
}

@Composable
fun PriceText(value: Double) {
    Text(
        NumberFormat.getCurrencyInstance().format(value),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.tertiary,
    )
}

@Composable
fun LodgingRating(rating: Double) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            "%.1f".format(rating),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

object LodgingSearchDestination {
    @Serializable
    data class Params(
        val tripId: String,
        val locationId: String,
        val checkIn: Long,
        val checkOut: Long,
        val timeZoneId: String,
    )
}

@Composable
fun LodgingSearch(
    navController: NavController,
    viewModel: LodgingSearchViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LodgingSearch(
        navController = navController,
        state = state,
        onLodgingTapped = { viewModel.onLodgingTapped(it.id) },
        onLodgingClosed = { viewModel.onLodgingClosed(it) },
        onAddLodgingTapped = { viewModel.onAddLodgingTapped(it) },
        onContinueBrowsingTapped = { viewModel.onContinueBrowsingTapped() },
        onSortOptionSelected = { viewModel.onSortOptionSelected(it) },
        onFiltersApplied = { minRating, minStars, priceRange ->
            viewModel.onFiltersApplied(minRating, minStars, priceRange)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLodgingSearch() {
    AppTheme {
        val results = List(10) { index ->
            LodgingSearchResultState(
                id = index.toString(),
                name = "Some super large Hotel name $index",
                address = "$index Street, City, ${index * 1023}",
                coverImage = "",
                rating = index * 1.2,
                lodgingType = "Hotel",
                price = index * 12.4,
            )
        }
        val details = results.take(5).associate { lodging ->
            lodging.id to LodgingDetailsState(
                name = lodging.name,
                rating = lodging.rating,
                reviewCountText = "",
                lodgingType = lodging.lodgingType,
                photos = listOf(lodging.coverImage),
                checkIn = Time("2025-08-10T00:00 -0500"),
                checkOut = Time("2025-08-15T00:00 -0500"),
                price = lodging.price,
                rooms = emptyList(),
                address = lodging.address,
                latitude = 0.0,
                longitude = 0.0,
                isLoading = false,
            )
        }.toMutableMap()
        val loadedState = LodgingSearchViewModel.UiState.Loaded(
            LodgingSearchViewModel.SearchParamsState(
                checkIn = Time("2025-08-10T00:00 -0500"),
                checkOut = Time("2025-08-15T00:00 -0500"),
                locationText = "New York, United States",
            ),
            sortAndFilterState = LodgingSearchViewModel.SortAndFilterState(),
            results = results,
            openedResults = details,
        )
        val loadingState = LodgingSearchViewModel.UiState.Loading(
            LodgingSearchViewModel.SearchParamsState(
                checkIn = Time("2025-08-10T00:00 -0500"),
                checkOut = Time("2025-08-15T00:00 -0500"),
                locationText = "New York, United States",
            ),
        )
        var state by remember { mutableStateOf<LodgingSearchViewModel.UiState>(loadedState) }
        LodgingSearch(
            navController = rememberNavController(),
            state = state,
            onLodgingTapped = { },
            onSortOptionSelected = {},
            onFiltersApplied = { _, _, _ -> },
            onLodgingClosed = {},
            onAddLodgingTapped = {},
            onContinueBrowsingTapped = {},
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    state = if (state == loadingState) loadedState else loadingState
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(if (state == loadingState) "Load" else "Reset")
            }
        }
    }
}

