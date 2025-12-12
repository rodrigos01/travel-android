package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import travel.vola.android.extensions.viewModel
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel

@Composable
fun TripCreationAssistant(navController: NavController) {
    val viewModel: TripCreationAssistantViewModel =
        viewModel(factory = TripCreationAssistantViewModel.Factory())
    val state by viewModel.uiState.collectAsState()
    TripCreationAssistant(state, onNavigateBack = { navController.popBackStack() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreationAssistant(
    state: TripCreationAssistantViewModel.UiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Trip Creation Assistant")
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            })
        }
    ) { contentPadding ->
        when (state) {
            is TripCreationAssistantViewModel.UiState.Generating -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = contentPadding.calculateTopPadding() + 16.dp,
                            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                            bottom = contentPadding.calculateBottomPadding() + 24.dp,
                            end = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                        )
                ) {
                    Text("Generating...", style = MaterialTheme.typography.titleLarge)
                }
            }

            is TripCreationAssistantViewModel.UiState.InitialParameters -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            top = contentPadding.calculateTopPadding() + 16.dp,
                            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                            bottom = contentPadding.calculateBottomPadding() + 24.dp,
                            end = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                        )
                ) {
                    InitialParameterOption("Occasions", state.occasions)
                    InitialParameterOption("Interests", state.interests)
                    InitialParameterOption("Vibe", state.vibe)
                    InitialParameterOption("Focus", state.focus)
                    InitialParameterOption("Duration", state.duration)
                    InitialParameterOption("Must Have", state.mustHave)
                }
            }
        }
    }
}

@Composable
private fun InitialParameterOption(title: String, options: List<String>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            options.forEach {
                FilterChip(selected = false, onClick = {}, label = {
                    Text(it)
                })
            }
        }
    }
}

object TripCreationAssistantDestination {
    const val ROUTE = "trip_creation_assistant"
}

@Composable
@Preview
fun TripCreationAssistantPreview() {
    MaterialTheme {
        TripCreationAssistant(
            TripCreationAssistantViewModel.UiState.InitialParameters(
                occasions = listOf("Workation", "Vacation", "Business Trip"),
                interests = listOf("Adventure", "Food", "Music", "Sports"),
                vibe = listOf("Chill", "Relax", "Party", "Study"),
                focus = listOf(
                    "Arctic Adventure",
                    "Design & Culture Tour",
                    "Winter Wellness Retreat",
                    "Culinary Exploration"
                ),
                duration = listOf("1-2 Weeks", "10-15 days", "7-10 days"),
                mustHave = listOf(
                    "Chasing the Aurora Borealis",
                    "Sleeping in an Ice Hotel",
                    "Traditional Sauna & Cold Plunge"
                )
            ),
            onNavigateBack = {}
        )
    }
}