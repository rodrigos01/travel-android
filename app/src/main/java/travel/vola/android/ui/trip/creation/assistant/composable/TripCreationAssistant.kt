package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import travel.vola.android.extensions.viewModel
import travel.vola.android.ui.theme.AppTheme
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
        onInitialParametersNextTapped = viewModel::onInitialParametersNextTapped,
        onFollowUpQuestionOptionTapped = viewModel::onFollowUpQuestionOptionTapped,
        onFollowUpQuestionsNextTapped = viewModel::onFollowUpQuestionsNextTapped,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripCreationAssistant(
    state: UiState,
    onNavigateBack: () -> Unit,
    onInitialParameterOptionTapped: (Int, UiState.OptionGroupType) -> Unit,
    onInitialParametersNextTapped: () -> Unit,
    onFollowUpQuestionOptionTapped: (Int, UiState.FollowUpQuestion) -> Unit,
    onFollowUpQuestionsNextTapped: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                val title = when (state) {
                    is UiState.Generating -> "Travel Creation Assistant"
                    is UiState.InitialParameters -> "Initial Parameters"
                    is UiState.InitialParametersFollowUp -> "Follow Up Questions"
                }
                Text(title)
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
        val paddingValues = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            end = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
        )
        when (state) {
            is UiState.Generating -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LoadingIndicator()
                    Text("Generating...", style = MaterialTheme.typography.titleLarge)
                }
            }

            is UiState.InitialParameters -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    state.optionGroups.forEach { group ->
                        OptionGroup(
                            title = when (group.type) {
                                UiState.OptionGroupType.OCCASIONS -> "Occasions"
                                UiState.OptionGroupType.INTERESTS -> "Interests"
                                UiState.OptionGroupType.VIBE -> "Vibe"
                                UiState.OptionGroupType.FOCUS -> "Focus"
                                UiState.OptionGroupType.DURATION -> "Duration"
                                UiState.OptionGroupType.MUST_HAVE -> "Must Have"
                            },
                        ) {
                            group.options.forEachIndexed { index, option ->
                                FilterChip(
                                    selected = option.isSelected,
                                    onClick = { onInitialParameterOptionTapped(index, group.type) },
                                    label = {
                                        Text(option.option)
                                    })
                            }
                        }
                    }
                    Button(
                        onClick = onInitialParametersNextTapped,
                        enabled = state.nextButtonEnabled,
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Next") }
                }
            }

            is UiState.InitialParametersFollowUp -> {
                if (state.questions.isEmpty()) {
                    Text("No Further questions", style = MaterialTheme.typography.titleLarge)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(paddingValues)
                    ) {
                        state.questions.forEachIndexed { index, question ->
                            if (index > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Text(
                                question.question,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            question.answers.forEachIndexed { optionIndex, option ->
                                ElevatedCard(
                                    onClick = {
                                        onFollowUpQuestionOptionTapped(
                                            optionIndex,
                                            question
                                        )
                                    },
                                    colors = if (option.isSelected) CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) else CardDefaults.elevatedCardColors(),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(option.option, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                        Button(
                            onClick = onFollowUpQuestionsNextTapped,
                            enabled = state.nextButtonEnabled,
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Next") }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionGroup(
    title: String,
    options: @Composable FlowRowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = options,
        )
    }
}

object TripCreationAssistantDestination {
    const val ROUTE = "trip_creation_assistant"
}

@Composable
@Preview
fun TripCreationAssistantPreview() {
    val initalParamtersState = UiState.InitialParameters(
        optionGroups = listOf(
            UiState.OptionGroup(
                UiState.OptionGroupType.OCCASIONS,
                listOfOptions("Workation", "Vacation", "Business Trip", "Family Trip")
            ),
            UiState.OptionGroup(
                UiState.OptionGroupType.INTERESTS,
                listOfOptions("Hiking", "Shopping", "Sightseeing")
            )
        )
    )
    val followUpState = UiState.InitialParametersFollowUp(
        questions = listOf(
            UiState.FollowUpQuestion(
                "What time of day would you prefer to work on weekdays?",
                listOfOptions(
                    "I prefer to have my work in the Morning, when I'm the most productive",
                    "Aternoons are my favoriote time for working",
                    "No need to dedicate time for work, I'll just wing it LOL",
                    selected = 0
                )
            ),
            UiState.FollowUpQuestion(
                "What time of day would you prefer to work on weekdays?",
                listOfOptions(
                    "I prefer to have my work in the Morning, when I'm the most productive",
                    "Aternoons are my favoriote time for working",
                    "No need to dedicate time for work, I'll just wing it LOL",
                    selected = 0
                )
            )
        ),
    )
    AppTheme {
        TripCreationAssistant(
            followUpState,
            onNavigateBack = {},
            onInitialParameterOptionTapped = { _, _ -> },
            onInitialParametersNextTapped = {},
            onFollowUpQuestionOptionTapped = { _, _ -> },
            onFollowUpQuestionsNextTapped = {},
        )
    }
}

private fun listOfOptions(vararg options: String, selected: Int = -1): List<UiState.Option> =
    options.mapIndexed { index, option -> UiState.Option(option, isSelected = index == selected) }