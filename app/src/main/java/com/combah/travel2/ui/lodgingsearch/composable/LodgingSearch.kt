package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.extensions.Time
import com.combah.travel2.ui.common.components.TabbedHost
import com.combah.travel2.ui.common.components.TabbedHostScope
import com.combah.travel2.ui.lodgingsearch.state.LodgingDetailsState
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import com.combah.travel2.ui.lodgingsearch.viewmodel.LodgingSearchViewModel
import com.combah.travel2.ui.theme.AppTheme
import kotlinx.serialization.Serializable
import java.text.NumberFormat

@Composable
fun LodgingSearch(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    onLodgingTapped: (LodgingSearchResultState) -> Unit,
    onLodgingClosed: (String) -> Unit = {},
) {
    val searchTabListState = rememberLazyListState()
    val searchResults: @Composable TabbedHostScope.() -> Unit = {
        LodgingSearchResults(navController,
            state,
            scrollState = searchTabListState,
            onLodgingTapped = { lodging ->
                onLodgingTapped(lodging)
                navigate(lodging.id)
            })
    }
    TabbedHost(startDestination = "search") {
        tab("search", icon = { Icon(Icons.Outlined.Search, contentDescription = null) }) {
            searchResults()
        }
        state.openedResults.forEach { (id, lodging) ->
            tab(id, title = { Text(lodging.name) }, content = {
                LodgingDetails(lodging, onClose = {
                    onLodgingClosed(id)
                    navigate("search")
                })
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingSearchResults(
    navController: NavController,
    state: LodgingSearchViewModel.UiState,
    onLodgingTapped: (LodgingSearchResultState) -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
) {
    Scaffold(topBar = {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
            TopAppBar(title = { Text("Lodging Search") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            })
            LodgingSearchParams(
                checkIn = state.checkIn,
                checkOut = state.checkOut,
                minCheckOut = state.minCheckOut,
                locationText = state.locationText,
                onCheckInDateSelected = {},
                onCheckOutDateSelected = {},
                onLocationSearchResultSelected = {},
                onLocationSearchTextChanged = {},
            )
        }
    }) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            items(state.results, key = { it.id }) { result ->
                Surface(shape = MaterialTheme.shapes.large,
                    border = BorderStroke(
                        1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
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
        val checkIn: Long,
        val checkOut: Long,
        val locationId: String,
        val locationName: String,
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
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLodgingSearch() {
    AppTheme {
        var details by remember { mutableStateOf(mapOf<String, LodgingDetailsState>()) }
        LodgingSearch(navController = rememberNavController(), LodgingSearchViewModel.UiState(
            checkIn = Time("2025-08-10T00:00 -0500"),
            checkOut = Time("2025-08-15T00:00 -0500"),
            locationText = "New York, United States",
            results = List(10) { index ->
                LodgingSearchResultState(
                    id = index.toString(),
                    name = "Hotel $index",
                    address = "$index Street, City, ${index * 1023}",
                    coverImage = "",
                    rating = index * 1.2,
                    lodgingType = "Hotel",
                    price = index * 12.4,
                )
            },
            openedResults = details
        ), onLodgingTapped = { lodging ->
            details = details.toMutableMap().also {
                it[lodging.id] = LodgingDetailsState(
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
                )
            }
        })
    }
}

