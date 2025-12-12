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
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState

@Composable
fun TripCreationAssistant(navController: NavController) {
    val viewModel: TripCreationAssistantViewModel =
        viewModel(factory = TripCreationAssistantViewModel.Factory())
    val state by viewModel.uiState.collectAsState()
    TripCreationAssistant(
        state,
        onNavigateBack = { navController.popBackStack() },
        onInitialParameterOptionTapped = viewModel::onInitialParameterOptionTapped,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCreationAssistant(
    state: UiState,
    onNavigateBack: () -> Unit,
    onInitialParameterOptionTapped: (Int, UiState.OptionGroupType) -> Unit,
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
            is UiState.Generating -> {
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

            is UiState.InitialParameters -> {
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
                    state.optionGroups.forEach { group ->
                        InitialParameterOption(
                            title = when (group.type) {
                                UiState.OptionGroupType.OCCASIONS -> "Occasions"
                                UiState.OptionGroupType.INTERESTS -> "Interests"
                                UiState.OptionGroupType.VIBE -> "Vibe"
                                UiState.OptionGroupType.FOCUS -> "Focus"
                                UiState.OptionGroupType.DURATION -> "Duration"
                                UiState.OptionGroupType.MUST_HAVE -> "Must Have"
                            },
                            options = group.options,
                            onOptionTapped = { onInitialParameterOptionTapped(it, group.type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialParameterOption(
    title: String,
    options: List<UiState.Option>,
    onOptionTapped: (Int) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            options.forEachIndexed { index, option ->
                FilterChip(
                    selected = option.isSelected,
                    onClick = { onOptionTapped(index) },
                    label = {
                        Text(option.option)
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
            UiState.InitialParameters(
                optionGroups = listOf(
                    UiState.OptionGroup(
                        UiState.OptionGroupType.OCCASIONS,
                        listOfOptions("Workation", "Vacation", "Business Trip")
                    )
                )
            ),
            onNavigateBack = {},
            onInitialParameterOptionTapped = { _, _ -> }
        )
    }
}

private fun listOfOptions(vararg options: String): List<UiState.Option> =
    options.map { UiState.Option(it) }