package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import travel.vola.android.R
import travel.vola.android.extensions.viewModel
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel
import travel.vola.android.ui.trip.creation.assistant.viewmodel.UiState
import java.time.ZonedDateTime

@Composable
fun TripCreationAssistant(params: TripCreationAssistantDestination.Params) {
    val viewModel: TripCreationAssistantViewModel =
        viewModel(
            factory = TripCreationAssistantViewModel.Factory(
                tripId = params.tripId,
                destinations = params.destinations,
                startDate = params.startDate,
                endDate = params.endDate,
            ),
        )
    val state by viewModel.uiState.collectAsState()
    TripCreationAssistant(
        state,
        onNavigateBack = viewModel::onNavigateBack,
        onSkipTapped = viewModel::onSkipTapped,
        onDestinationSearchTextChanged = viewModel::onDestinationSearchTextChanged,
        onDestinationSearchResultSelected = viewModel::onDestinationSearchResultSelected,
        onDestinationClearTapped = viewModel::onDestinationClearTapped,
        onFixedDatesSet = viewModel::onFixedDatesSet,
        onStartDateSet = viewModel::onStartDateSet,
        onEndDateSet = viewModel::onEndDateSet,
        onGroupTypeSet = viewModel::onGroupTypeSet,
        onTravelersSet = viewModel::onTravelersSet,
        onBasicInformationNextTapped = viewModel::onBasicInformationNextTapped,
        onInitialParameterOptionTapped = viewModel::onInitialParameterOptionTapped,
        onInitialParametersOptionAdded = viewModel::onInitialParametersOptionAdded,
        onInitialParametersAnythingElseTextChanged = viewModel::onInitialParametersAnythingElseUpdated,
        onInitialParametersNextTapped = viewModel::onInitialParametersNextTapped,
        onFollowUpQuestionOptionTapped = viewModel::onFollowUpQuestionOptionTapped,
        onFollowUpQuestionCustomAnswerAdded = viewModel::onFollowUpQuestionCustomAnswerAdded,
        onFollowUpQuestionsNextTapped = viewModel::onFollowUpQuestionsNextTapped,
        onRetryTapped = viewModel::onRetryTapped,
        onItinerarySelected = viewModel::onItinerarySelected,
        onConfirmationOptionTapped = viewModel::onConfirmationOptionSelected,
        onCreateTripTapped = viewModel::onCreateTripTapped,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripCreationAssistant(
    state: UiState,
    onNavigateBack: () -> Unit,
    onSkipTapped: () -> Unit,
    onDestinationSearchTextChanged: (CharSequence) -> Unit,
    onDestinationClearTapped: (Int) -> Unit,
    onDestinationSearchResultSelected: (Int) -> Unit,
    onFixedDatesSet: (Boolean) -> Unit,
    onStartDateSet: (ZonedDateTime) -> Unit,
    onEndDateSet: (ZonedDateTime) -> Unit,
    onGroupTypeSet: (UiState.TravelGroupType) -> Unit,
    onTravelersSet: (Int?) -> Unit,
    onBasicInformationNextTapped: () -> Unit,
    onInitialParameterOptionTapped: (Int, UiState.OptionGroupType) -> Unit,
    onInitialParametersOptionAdded: (UiState.OptionGroupType, String) -> Unit,
    onInitialParametersAnythingElseTextChanged: (String) -> Unit,
    onInitialParametersNextTapped: () -> Unit,
    onFollowUpQuestionOptionTapped: (Int, UiState.FollowUpQuestion) -> Unit,
    onFollowUpQuestionCustomAnswerAdded: (String, UiState.FollowUpQuestion) -> Unit,
    onFollowUpQuestionsNextTapped: () -> Unit,
    onItinerarySelected: (UiState.Itinerary?) -> Unit,
    onConfirmationOptionTapped: (String) -> Unit,
    onCreateTripTapped: (UiState.Itinerary) -> Unit,
    onRetryTapped: () -> Unit,
) {
    BackHandler(onBack = onNavigateBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (state) {
                        is UiState.Error,
                        is UiState.Generating,
                        is UiState.BasicInformation,
                        -> "Travel Creation Assistant"

                        is UiState.InitialParameters -> "Initial Parameters"
                        is UiState.InitialParametersFollowUp -> "Follow Up Questions"
                        is UiState.HighLevelItineraryOptions -> "High Level Itinerary Options"
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "",
                        )
                    }
                },
                actions = {
                    if (state.skipEnabled) {
                        TextButton(onClick = onSkipTapped) {
                            Text(stringResource(R.string.action_skip))
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        val paddingValues = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            end = contentPadding.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
        )
        when (state) {
            is UiState.Error -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "error",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        "There was an error generating content",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Button(onClick = onRetryTapped) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            is UiState.Generating -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    LoadingIndicator()
                    Text("Generating...", style = MaterialTheme.typography.titleLarge)
                }
            }

            is UiState.BasicInformation -> {
                BasicInformationForm(
                    state,
                    onDestinationSearchTextChanged,
                    onDestinationSearchResultSelected,
                    onDestinationClearTapped,
                    onFixedDatesSet,
                    onStartDateSet,
                    onEndDateSet,
                    onGroupTypeSet,
                    onTravelersSet,
                    onBasicInformationNextTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                )
            }

            is UiState.InitialParameters -> {
                InitialParameters(
                    state,
                    onInitialParameterOptionTapped,
                    onOptionAdded = onInitialParametersOptionAdded,
                    onAnythingElseTextChanged = onInitialParametersAnythingElseTextChanged,
                    onInitialParametersNextTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                )
            }

            is UiState.InitialParametersFollowUp -> {
                InitialParametersFollowUp(
                    state,
                    onFollowUpQuestionOptionTapped,
                    onFollowUpQuestionCustomAnswerAdded,
                    onFollowUpQuestionsNextTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                )
            }

            is UiState.HighLevelItineraryOptions -> {
                HighLevelItineraryOptions(
                    state,
                    onItinerarySelected,
                    onConfirmationOptionTapped,
                    onCreateTripTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                )
            }
        }
    }
}

object TripCreationAssistantDestination {
    const val ROUTE = "trip_creation_assistant"

    const val RESULT_KEY_FINISHED_STATUS = "result_finished_status"

    enum class FinishedStatus {
        NONE,
        COMPLETED,
        CANCELLED,
    }

    @Serializable
    data class Params(
        val tripId: String? = null,
        val destinations: List<String> = emptyList(),
        val startDate: String? = null,
        val endDate: String? = null,
    )
}

@Composable
@Preview
fun TripCreationAssistantPreview() {
    AppTheme {
        TripCreationAssistant(
            UiState.Generating,
            onNavigateBack = {},
            onSkipTapped = {},
            onDestinationSearchTextChanged = {},
            onDestinationSearchResultSelected = {},
            onDestinationClearTapped = {},
            onFixedDatesSet = {},
            onStartDateSet = {},
            onEndDateSet = {},
            onGroupTypeSet = {},
            onTravelersSet = {},
            onBasicInformationNextTapped = {},
            onInitialParameterOptionTapped = { _, _ -> },
            onInitialParametersOptionAdded = { _, _ -> },
            onInitialParametersAnythingElseTextChanged = {},
            onInitialParametersNextTapped = {},
            onFollowUpQuestionOptionTapped = { _, _ -> },
            onFollowUpQuestionCustomAnswerAdded = { _, _ -> },
            onFollowUpQuestionsNextTapped = {},
            onRetryTapped = {},
            onItinerarySelected = {},
            onConfirmationOptionTapped = {},
            onCreateTripTapped = {},
        )
    }
}
