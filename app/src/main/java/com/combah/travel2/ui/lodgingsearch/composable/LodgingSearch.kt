package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.combah.travel2.extensions.Time
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.lodgingsearch.state.LodgingSearchResultState
import com.combah.travel2.ui.theme.AppTheme
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingSearch(
    navController: NavController,
    checkIn: Time,
    checkOut: Time,
    locationText: String,
    minCheckIn: Time? = null,
    minCheckOut: Time? = null,
    results: List<LodgingSearchResultState>,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column {
                TopAppBar(title = { Text("Lodging Search") }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                })
                LodgingSearchParams(
                    checkIn = checkIn,
                    minCheckIn = minCheckIn,
                    checkOut = checkOut,
                    minCheckOut = minCheckOut,
                    locationText = locationText,
                    onCheckInDateSelected = {},
                    onCheckOutDateSelected = {},
                    onLocationSearchResultSelected = {},
                    onLocationSearchTextChanged = {},
                )
            }
        }) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 16.dp),
        ) {
            items(results, key = { it.id }) { result ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.large,
                        )
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(model = result.coverImage),
                            contentDescription = "Place Description",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.77f)
                                .background(color = MaterialTheme.colorScheme.tertiary),
                            contentScale = ContentScale.FillWidth
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
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(MaterialTheme.shapes.extraLarge)
                                        .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                )
                                {
                                    Text(
                                        "%.1f".format(result.rating),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                    )
                                }
                                Text(
                                    result.lodgingType,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance().format(result.price),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLodgingSearch() {
    AppTheme {
        LodgingSearch(
            navController = rememberNavController(),
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
        })
    }
}

