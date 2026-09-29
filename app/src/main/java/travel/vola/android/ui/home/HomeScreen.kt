package travel.vola.android.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import travel.vola.android.R
import travel.vola.android.extensions.viewModel
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name_vola)) },
                actions = {
                    IconButton(onClick = viewModel::signOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.action_sign_out),
                        )
                    }
                },
            )
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
