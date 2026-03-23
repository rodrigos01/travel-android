package travel.vola.android.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import travel.vola.android.R
import travel.vola.android.extensions.viewModel
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.ui.trip.creation.assistant.composable.TripCreationAssistantDestination
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import travel.vola.android.ui.triplist.composable.TripList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(),
    )
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    Scaffold(
        topBar = {
            var showDataSourceSelector by remember { mutableStateOf(false) }
            TopAppBar(
                title = { Text(stringResource(R.string.app_name_vola)) },
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDataSourceSelector = true
                    },
                ),
            )
            DropdownMenu(
                expanded = showDataSourceSelector,
                onDismissRequest = { showDataSourceSelector = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.source_local)) },
                    onClick = {
                        viewModel.onDataSourceChanged(DataSourceType.LOCAL)
                        showDataSourceSelector = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.source_firebase)) },
                    onClick = {
                        viewModel.onDataSourceChanged(DataSourceType.FIREBASE)
                        showDataSourceSelector = false
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(TripCreationAssistantDestination.ROUTE)
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        },
    ) { paddingValues ->
        TripList(
            state = uiState.tripListState,
            onTripClicked = { tripId ->
                navController.navigate(TripDetailsDestination.getRoute(tripId))
            },
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 16.dp,
            ),
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}

object HomeScreenDestination {
    const val ROUTE = "home"
}
