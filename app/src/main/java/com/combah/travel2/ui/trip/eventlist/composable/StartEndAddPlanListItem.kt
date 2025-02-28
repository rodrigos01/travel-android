package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.Time
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState

data class StartEndAddPlanListItemState(
    val startState: AddPlanRowState,
    val endState: AddPlanRowState,
)

@Composable
fun rememberStartEndAddPlanListItemState(
    startState: AddPlanRowState,
    endState: AddPlanRowState,
) = remember { StartEndAddPlanListItemState(startState, endState) }

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
    showEndTextField: Boolean = true,
    endLabelText: String? = null,
    endPlaceHolder: String? = null,
    onEndTextChanged: (CharSequence) -> Unit = {},
) {
    Column {
        AddPlanRow(
            state = state.startState,
            minTime = uiState.startState.minTime,
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
            minTime = uiState.endState.minTime,
            searchResults = uiState.endState.searchResults,
            title = endTitle,
            timeSelectorLabel = endTimeSelectorLabel,
            dateSelectionEnabled = uiState.endState.dateSelectionEnabled,
            placeHolder = endPlaceHolder,
            showTextField = showEndTextField,
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
                        time = Time("2025-09-21T14:49 +0100"),
                        minTime = Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    endState = ManualAddPlanState(
                        time = null,
                        minTime = Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList()
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
