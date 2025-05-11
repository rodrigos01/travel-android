package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.Time
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState

data class StartEndAddPlanListItemState(
    val startState: AddPlanRowState,
    val endState: AddPlanRowState,
)

@Composable
fun rememberStartEndAddPlanListItemState(
    startState: AddPlanRowState = rememberAddPlanRowState(),
    endState: AddPlanRowState = rememberAddPlanRowState(),
) = remember(startState, endState) { StartEndAddPlanListItemState(startState, endState) }

@Composable
fun StartEndAddPlanListItem(
    state: StartEndAddPlanListItemState,
    uiState: ManualStartEndAddPlanState,
    startTitle: @Composable () -> Unit,
    startTimeSelectorLabel: String,
    startLabelText: String? = null,
    startPlaceHolder: String? = null,
    onStartTextChanged: (CharSequence) -> Unit,
    endTitle: @Composable () -> Unit,
    endTimeSelectorLabel: String,
    showEndTimePickerButton: Boolean = true,
    endLabelText: String? = null,
    endPlaceHolder: String? = null,
    onEndTextChanged: (CharSequence) -> Unit = {},
) {
    Column {
        AddPlanRow(
            state = state.startState,
            minTime = uiState.startState.minDateTime,
            searchResults = uiState.startState.searchResults,
            title = startTitle,
            timeSelectorLabel = startTimeSelectorLabel,
            dateSelectionEnabled = uiState.startState.dateSelectionEnabled,
            placeHolder = startPlaceHolder,
            labelText = startLabelText,
            text = uiState.startState.locationText,
            onTextChanged = onStartTextChanged,
        )
        AddPlanRow(
            state = state.endState,
            minTime = uiState.endState.minDateTime,
            searchResults = uiState.endState.searchResults,
            title = endTitle,
            timeSelectorLabel = endTimeSelectorLabel,
            dateSelectionEnabled = uiState.endState.dateSelectionEnabled,
            placeHolder = endPlaceHolder,
            showTextField = showEndTimePickerButton,
            labelText = endLabelText,
            text = uiState.endState.locationText,
            onTextChanged = onEndTextChanged,
        )
    }
}

@Composable
@Preview
fun StartEndAddPlanListItemPreview() {
    AppTheme {
        Surface {
            StartEndAddPlanListItem(
                state = StartEndAddPlanListItemState(
                    startState = rememberAddPlanRowState(),
                    endState = rememberAddPlanRowState()
                ),
                uiState = AddFlightItemState(
                    id = "",
                    timestamp = Time.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = Time("2025-09-21T14:49 +0100"),
                        minDateTime = Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    endState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                startTitle = { Text("Start") },
                startTimeSelectorLabel = "Pick Start Time",
                startPlaceHolder = "Enter Start point",
                startLabelText = "Start",
                onStartTextChanged = {},
                endTitle = { Text("End") },
                endTimeSelectorLabel = "Pick End Time",
                endPlaceHolder = "Enter End point",
                endLabelText = "End",
                onEndTextChanged = {},
            )
        }
    }
}
