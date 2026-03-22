package travel.vola.android.ui.lodgingsearch.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import travel.vola.android.R
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.common.ui.components.Map
import travel.vola.android.common.ui.components.MapScaffold
import travel.vola.android.common.ui.components.MapScaffoldState
import travel.vola.android.common.ui.components.OverlayHostProvider
import travel.vola.android.common.ui.components.TabBar
import travel.vola.android.common.ui.components.isLargeScreen
import travel.vola.android.common.ui.components.mapMarkerIcon
import travel.vola.android.common.ui.components.rememberMapScaffoldState
import travel.vola.android.common.ui.modifier.skeletonLoader
import travel.vola.android.common.ui.preview.TabletPreview
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.common.ui.state.MarkerViewState
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.extensions.viewModel
import travel.vola.android.ui.lodgingsearch.state.LodgingDetailsState
import travel.vola.android.ui.lodgingsearch.state.LodgingRoomOfferState
import travel.vola.android.ui.lodgingsearch.state.LodgingSearchResultState
import travel.vola.android.ui.lodgingsearch.viewmodel.LodgingSearchViewModel
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.ConfirmationDialog
import java.text.NumberFormat

private const val SEARCH_TAB_ID = "search"

@Composable
private fun LodgingSearch(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    onLodgingTapped: (LodgingSearchResultState?) -> Unit,
    onLodgingClosed: (String) -> Unit,
    onAddLodgingTapped: (String) -> Unit,
    onContinueBrowsingTapped: () -> Unit,
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit,
    onFiltersApplied: (minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>) -> Unit,
    onRetryTapped: () -> Unit,
    showMap: Boolean = false,
) {
    if (state.localState.showAddConfirmation) {
        ConfirmationDialog(
            onConfirm = { navController.popBackStack() },
            onDismiss = onContinueBrowsingTapped,
            confirmButtonLabel = stringResource(R.string.action_back_to_trip),
            dismissButtonLabel = stringResource(R.string.action_continue_browsing)
        ) {
            Text(stringResource(R.string.dialog_lodging_added_continue_browsing))
        }
    }
    val mapScaffoldState = rememberMapScaffoldState(mapInitiallyVisible = showMap)
    val tabBarListState = rememberLazyListState()
    val loadedState = state as? LodgingSearchViewModel.UiState.Loaded
    val openedResult = loadedState?.selectedResult
    val openedResultId = openedResult?.id
    LaunchedEffect(openedResultId) {
        if (openedResultId != null) {
            tabBarListState.animateScrollToItem(state.openedResults.keys.indexOf(openedResultId))
        }
    }
    OverlayHostProvider {
        ResultsWithMap(
            navController,
            mapScaffoldState,
            tabBarListState,
            state,
            openedResultId,
            openedResult,
            onLodgingTapped = { lodging ->
                onLodgingTapped(lodging)
            },
            onLodgingClosed,
            onAddLodgingTapped,
            onSortOptionSelected,
            onFiltersApplied,
            onRetryTapped,
        )
    }
}

enum class ControlsVisible {
    NONE, FILTERS, SORT,
}

@Composable
fun ResultsWithMap(
    navController: NavController,
    mapScaffoldState: MapScaffoldState,
    tabBarScrollState: LazyListState,
    state: LodgingSearchViewModel.UiState,
    openedResultId: String?,
    openedResult: LodgingDetailsState?,
    onLodgingTapped: (LodgingSearchResultState?) -> Unit,
    onLodgingClosed: (String) -> Unit,
    onAddLodgingTapped: (String) -> Unit,
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit,
    onFiltersApplied: (minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>) -> Unit,
    onRetryTapped: () -> Unit,
) {
    val loadedState = state as? LodgingSearchViewModel.UiState.Loaded
    var selectedId by remember(openedResultId?.takeIf { mapScaffoldState.sizeClass.isLargeScreen }) {
        mutableStateOf(
            openedResultId
        )
    }
    val markers = loadedState?.results?.map {
        MarkerViewState(
            position = Pair(it.latitude, it.longitude),
            name = it.name,
            type = MarkerType.Lodging,
            selected = it.id == selectedId,
        )
    } ?: emptyList()
    val boundsMarkers = openedResult?.takeIf { mapScaffoldState.sizeClass.isLargeScreen }?.let {
        listOf(LatLng(it.latitude, it.longitude))
    } ?: markers.firstOrNull { it.selected }
        ?.let { listOf(LatLng(it.position.first, it.position.second)) } ?: emptyList()

    val coroutineScope = rememberCoroutineScope()
    var controlsVisible by remember { mutableStateOf(ControlsVisible.NONE) }
    val resultsScrollState = rememberLazyListState()
    val mapResultsScrollState = rememberLazyListState()
    val detailsContentStates = remember { mutableStateMapOf<String, LodgingDetailsContentState>() }
    val detailsContentState = detailsContentStates.getOrPut(openedResultId ?: SEARCH_TAB_ID) {
        rememberLodgingDetailsContentState()
    }
    LaunchedEffect(selectedId) {
        if (selectedId != null) {
            loadedState?.results?.indexOfFirst { it.id == selectedId }?.let { selectedIndex ->
                launch {
                    mapResultsScrollState.animateScrollToItem(selectedIndex)
                }
                if (mapScaffoldState.sizeClass.isLargeScreen) {
                    launch {
                        resultsScrollState.animateScrollToItem(selectedIndex)
                    }
                }
            }
        }
    }
    MapScaffold(
        state = mapScaffoldState,
        markers = markers,
        boundsPoints = boundsMarkers,
        onMarkerTapped = { marker ->
            val index = markers.indexOf(marker)
            selectedId = loadedState?.results?.getOrNull(index)?.id
        },
        minZoom = 17F,
        markerDescriptor = { marker ->
            val index = markers.indexOf(marker)
            val bitmap = loadedState?.results?.getOrNull(index)?.let {
                LodgingSearchMarkerIcon(
                    NumberFormat.getCurrencyInstance()
                    .apply { maximumFractionDigits = 0 }.format(it.price), marker.selected
                )
            } ?: mapMarkerIcon(MarkerType.Lodging, selected = marker.selected)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        },
        topBar = {
            if (!mapScaffoldState.sizeClass.isLargeScreen && loadedState?.selectedResult != null) {
                LodgingDetailsTopBar(
                    loadedState.selectedResult,
                    detailsContentState,
                    coroutineScope,
                    onClose = {
                        onLodgingClosed(loadedState.selectedResult.id)
                    },
                )
            } else if (!mapScaffoldState.sizeClass.isLargeScreen && mapScaffoldState.showMap) {
                MapTopBar(
                    navController,
                    onListButtonTapped = {
                        mapScaffoldState.showMap = false
                    },
                )
            } else if (mapScaffoldState.sizeClass.isLargeScreen || !mapScaffoldState.showMap) {
                SearchTopBar(
                    navController,
                    state,
                    controlsVisible,
                    onFiltersApplied,
                    onSortOptionSelected,
                    coroutineScope,
                    resultsScrollState,
                    showMapSwitchButton = !mapScaffoldState.sizeClass.isLargeScreen,
                    onMapButtonTapped = {
                        mapScaffoldState.showMap = true
                    },
                )
            }
        },
        bottomBar = {
            val openedResults = loadedState?.openedResults ?: emptyMap()
            AnimatedVisibility(openedResults.isNotEmpty()) {
                TabBar(
                    tabBarListState = tabBarScrollState, onTabClick = { tabId ->
                        val lodgingId = loadedState?.results?.firstOrNull { it.id == tabId }
                        onLodgingTapped(lodgingId)
                    }, modifier = Modifier
                        .padding(
                            bottom = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateBottomPadding()
                        )
                        .fillMaxWidth()
                ) {
                    tab(
                        SEARCH_TAB_ID,
                        selected = loadedState?.selectedResult == null,
                        icon = { Icon(Icons.Outlined.Search, contentDescription = null) })
                    openedResults.forEach { (tabId, lodging) ->
                        tab(
                            id = tabId,
                            selected = openedResultId == tabId,
                            title = { Text(stringResource(R.string.title_lodging_search)) },
                        )
                    }
                }
            }
        },
        mapContent = {
            if (!mapScaffoldState.sizeClass.isLargeScreen) {
                MapSearchResults(
                    state,
                    mapResultsScrollState,
                    onLodgingTapped,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        },
        content = { paddingValues ->
            Content(state, resultsScrollState, paddingValues, onLodgingTapped, onRetryTapped)
        },
        additionalContent = { paddingValues ->
            if (openedResultId != null && openedResult != null) {
                if (mapScaffoldState.sizeClass.isLargeScreen) {
                    LodgingDetails(
                        state = openedResult,
                        onClose = {
                            onLodgingClosed(openedResultId)
                        },
                        onAddToTripTapped = {
                            onAddLodgingTapped(openedResultId)
                        },
                        showMap = false,
                    )
                } else {
                    LodgingDetailsContent(
                        paddingValues,
                        openedResult,
                        onAddToTripTapped = {
                            onAddLodgingTapped(openedResultId)
                        },
                        showMap = true,
                        contentState = detailsContentState,
                        coroutineScope = coroutineScope,
                    )
                }
            }
        })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchTopBar(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    controlsVisible: ControlsVisible,
    onFiltersApplied: (minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>) -> Unit,
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit,
    coroutineScope: CoroutineScope,
    resultsScrollState: LazyListState,
    showMapSwitchButton: Boolean,
    onMapButtonTapped: () -> Unit,
) {
    var controlsVisible1 = controlsVisible
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(bottom = 8.dp)
            .animateContentSize()
    ) {
        TopAppBar(title = { Text("Lodging Search") }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = ""
                )
            }
        }, actions = {
            if (showMapSwitchButton) {
                IconButton(onClick = onMapButtonTapped) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                    )
                }
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
                controlsVisible1 = ControlsVisible.FILTERS
            }) {
                Icon(
                    painterResource(R.drawable.tune_baseline_24), contentDescription = null
                )
                Text(stringResource(R.string.action_filter))
            }
            TextButton(onClick = {
                controlsVisible1 = ControlsVisible.SORT
            }) {
                Icon(
                    painterResource(R.drawable.sort_baseline_24), contentDescription = null
                )
                Text(stringResource(R.string.action_sort))
            }
        }
        AnimatedContent(
            targetState = controlsVisible1,
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
                                controlsVisible1 = ControlsVisible.NONE
                            }) {
                                Text(
                                    "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = {
                                controlsVisible1 = ControlsVisible.NONE
                                onFiltersApplied(
                                    filterState.minRating.toDouble(),
                                    filterState.minStars,
                                    filterState.priceRange.start.toDouble()..filterState.priceRange.endInclusive.toDouble(),
                                )
                            }) {
                                Text(stringResource(R.string.action_apply))
                            }
                        }
                    }
                }

                ControlsVisible.SORT -> SortOptionSelector(
                    state.sortAndFilterState,
                    onSortOptionSelected = { option ->
                        onSortOptionSelected(option)
                        controlsVisible1 = ControlsVisible.NONE
                        coroutineScope.launch {
                            resultsScrollState.animateScrollToItem(0)
                        }
                    })

                ControlsVisible.NONE -> {}
            }
        }
    }
}

@Composable
fun MapTopBar(navController: NavController, onListButtonTapped: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 8.dp),
    ) {
        FilledTonalIconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = ""
            )
        }
        FilledTonalIconButton(onClick = onListButtonTapped) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.List,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun MapSearchResults(
    state: LodgingSearchViewModel.UiState,
    scrollState: LazyListState,
    onLodgingTapped: (LodgingSearchResultState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(all = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        when (state) {
            is LodgingSearchViewModel.UiState.Loading -> {
                Loading()
            }

            is LodgingSearchViewModel.UiState.Loaded -> {
                Loaded(
                    state,
                    onLodgingTapped,
                    itemStyle = LodgingSearchResultListItemStyle.Compact,
                    itemModifier = Modifier.width(screenWidth - 32.dp),
                )
            }
        }
    }
}

@Composable
private fun Content(
    state: LodgingSearchViewModel.UiState,
    resultsScrollState: LazyListState,
    paddingValues: PaddingValues,
    onLodgingTapped: (LodgingSearchResultState?) -> Unit,
    onRetryTapped: () -> Unit,
) {
    when (state) {
        is LodgingSearchViewModel.UiState.Loading -> {
            ResultsColumn(resultsScrollState, paddingValues) {
                Loading()
            }
        }

        is LodgingSearchViewModel.UiState.Loaded -> {
            ResultsColumn(resultsScrollState, paddingValues) {
                Loaded(state, onLodgingTapped)
            }
        }

        is LodgingSearchViewModel.UiState.Error -> {
            LoadingError(paddingValues, onRetryTapped)
        }
    }
}

@Composable
private fun ResultsColumn(
    scrollState: LazyListState,
    paddingValues: PaddingValues,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = scrollState,
        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        content = content,
    )
}

private fun LazyListScope.Loaded(
    state: LodgingSearchViewModel.UiState.Loaded,
    onLodgingTapped: (LodgingSearchResultState) -> Unit,
    itemStyle: LodgingSearchResultListItemStyle = LodgingSearchResultListItemStyle.Expanded,
    itemModifier: Modifier = Modifier,
) {
    items(state.results, key = { it.id }) { result ->
        LodgingSearchResultListItem(
            result,
            style = itemStyle,
            modifier = itemModifier
                .animateItem()
                .clickable(onClick = { onLodgingTapped(result) })
        )
    }
}

private fun LazyListScope.Loading() {
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
private fun LoadingError(
    paddingValues: PaddingValues,
    onRetryTapped: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
    ) {
        Text(stringResource(R.string.error_something_went_wrong))
        IconTextButton(onClick = onRetryTapped) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(stringResource(R.string.action_retry))
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
    params: LodgingSearchDestination.Params,
    navController: NavController,
) {
    val viewModel: LodgingSearchViewModel = viewModel(
        factory = LodgingSearchViewModel.Factory(params)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LodgingSearch(
        navController = navController,
        state = state,
        onLodgingTapped = { viewModel.onLodgingTapped(it?.id) },
        onLodgingClosed = { viewModel.onLodgingClosed(it) },
        onAddLodgingTapped = { viewModel.onAddLodgingTapped(it) },
        onContinueBrowsingTapped = { viewModel.onContinueBrowsingTapped() },
        onSortOptionSelected = { viewModel.onSortOptionSelected(it) },
        onFiltersApplied = { minRating, minStars, priceRange ->
            viewModel.onFiltersApplied(minRating, minStars, priceRange)
        },
        onRetryTapped = { viewModel.onRetryTapped() },
    )
}

@Composable
@Preview(group = "Phone", showSystemUi = true)
fun LodgingSearchPreview(showMap: Boolean = false, initialSelectedResult: String? = null) {
    AppTheme {
        var selectedResultId by remember { mutableStateOf(initialSelectedResult) }
        val results = List(10) { index ->
            val latMultipliers = listOf(1, 0, -1)
            val lonMultipliers = listOf(0, 1, -1)
            LodgingSearchResultState(
                id = index.toString(),
                name = "Some super large Hotel name $index",
                address = "$index Street, City, ${index * 1023}",
                coverImage = "https://photo.hotellook.com/image_v2/limit/h374703_0/1024/768.auto",
                rating = index * 1.2,
                reviewCount = index * 1234,
                lodgingType = "Hotel",
                price = (index + 1) * 123.4,
                latitude = 40.7453466 + index * 0.0005 * latMultipliers[index % latMultipliers.size],
                longitude = -73.9899909 + index * 0.0005 * latMultipliers[index % lonMultipliers.size],
            )
        }
        val details = remember { mutableStateMapOf<String, LodgingDetailsState>() }
        val state by remember {
            derivedStateOf {
                LodgingSearchViewModel.UiState.Loaded(
                    LodgingSearchViewModel.SearchParamsState(
                        checkIn = zonedDateTime("2025-08-10T00:00 -0500"),
                        checkOut = zonedDateTime("2025-08-15T00:00 -0500"),
                        locationText = "New York, United States",
                    ),
                    sortAndFilterState = LodgingSearchViewModel.SortAndFilterState(),
                    results = results,
                    openedResults = details,
                    selectedResult = details[selectedResultId]
                )
            }
        }
        LodgingSearch(
            navController = rememberNavController(),
            state = LodgingSearchViewModel.UiState.Error(
                state.searchState, state.localState, state.sortAndFilterState
            ),
            onLodgingTapped = { lodging ->
                selectedResultId = lodging?.id
                lodging?.let {
                    details[lodging.id] = LodgingDetailsState(
                        id = lodging.id,
                        name = lodging.name,
                        rating = lodging.rating,
                        reviewCount = lodging.reviewCount,
                        lodgingType = lodging.lodgingType,
                        photos = listOf(lodging.coverImage) + List(44, { index ->
                            "https://photo.hotellook.com/image_v2/limit/h374703_${(index + 1) % 23}/1024/768.auto"
                        }),
                        checkIn = zonedDateTime("2025-08-10T00:00 -0500"),
                        checkOut = zonedDateTime("2025-08-15T00:00 -0500"),
                        price = lodging.price,
                        rooms = List(2) {
                            LodgingRoomOfferState(
                                photos = emptyList(),
                                description = "Room description",
                                false,
                                false,
                                false,
                                false,
                                123.0,
                                "",
                                "Expedia",
                            )
                        },
                        address = lodging.address,
                        description = null,
                        latitude = lodging.latitude,
                        longitude = lodging.longitude,
                        isLoading = false,
                    )
                }
            },
            onSortOptionSelected = {},
            onFiltersApplied = { _, _, _ -> },
            onLodgingClosed = {
                selectedResultId = null
                details.remove(it)
            },
            onAddLodgingTapped = {},
            onContinueBrowsingTapped = {},
            onRetryTapped = {},
            showMap = showMap,
        )
    }
}

@Composable
@Preview(group = "Phone", showSystemUi = true)
fun LodgingSearchPreviewSelected() {
    LodgingSearchPreview(initialSelectedResult = "3")
}

@Composable
@Preview(group = "Phone", showSystemUi = true)
fun LodgingSearchPreviewWithMap() {
    LodgingSearchPreview(showMap = true)
}

@Composable
@TabletPreview
fun LodgingSearchPreviewTablet() {
    LodgingSearchPreview(initialSelectedResult = "3")
}
