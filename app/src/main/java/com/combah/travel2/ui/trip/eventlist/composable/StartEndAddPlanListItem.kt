package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState

data class StartEndAddPlanListItemState(
    val startState: AddPlanRowState<String>,
    val endState: AddPlanRowState<String>,
)

@Composable
fun rememberStartEndAddPlanListItemState(
    startState: AddPlanRowState<String>,
    endState: AddPlanRowState<String>,
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
            title = startTitle,
            state = state.startState,
            timeSelectorLabel = startTimeSelectorLabel,
            dateSelectionEnabled = uiState.startState.dateSelectionEnabled,
            placeHolder = startPlaceHolder,
            labelText = startLabelText,
            text = uiState.endState.locationText,
            onTextChanged = onStartTextChanged,
            searchResultItemContent = { it },
        )
        AddPlanRow(
            title = endTitle,
            state = state.endState,
            timeSelectorLabel = endTimeSelectorLabel,
            dateSelectionEnabled = uiState.endState.dateSelectionEnabled,
            placeHolder = endPlaceHolder,
            showTextField = showEndTextField,
            labelText = endLabelText,
            text = uiState.endState.locationText,
            onTextChanged = onEndTextChanged,
            searchResultItemContent = { it },
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
                        time = null,
                        minTime = Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    endState = ManualAddPlanState(
                        time = null,
                        minTime = Time.now(),
                        dateSelectionEnabled = true,
                        locationText = null,
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
