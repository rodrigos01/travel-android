package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.Time
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState

@Composable
fun AddFlightListItem(
    uiState: ManualStartEndAddPlanState,
    startEndAddPlanState: StartEndAddPlanListItemState,
    onAirportFromTextChanged: (CharSequence) -> Unit,
    onAirportToTextChanged: (CharSequence) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        state = startEndAddPlanState,
        startTitle = { Text("Departure") },
        startTimeSelectorLabel = "Choose Departure Time",
        startLabelText = "from",
        startPlaceHolder = "Enter City or Airport",
        onStartTextChanged = onAirportFromTextChanged,
        endTitle = { Text("Arrival") },
        endTimeSelectorLabel = "Choose Arrival Time",
        endLabelText = "to",
        endPlaceHolder = "Enter City or Airport",
        onEndTextChanged = onAirportToTextChanged,
    )
}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        Surface {
            AddFlightListItem(
                uiState = AddFlightItemState(
                    "", Time.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        time = Time("2025-06-12T05:00 -0300"),
                        minTime = Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    endState = ManualAddPlanState(
                        time = Time("2025-06-12T05:00 -0300"),
                        minTime = Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList()
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                startEndAddPlanState = rememberStartEndAddPlanListItemState(
                    rememberAddPlanRowState(),
                    rememberAddPlanRowState()
                ),
                onAirportFromTextChanged = {},
                onAirportToTextChanged = {},
            )
        }
    }
}
