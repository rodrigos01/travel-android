package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState

@Composable
fun AddLodgingListItem(
    uiState: ManualStartEndAddPlanState,
    startEndAddPlanState: StartEndAddPlanListItemState,
    onLodgingTextChanged: (CharSequence) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        state = startEndAddPlanState,
        startTitle = { Text("CheckIn") },
        startTimeSelectorLabel = "Check-in Time",
        startLabelText = "Lodging Name",
        startPlaceHolder = "Enter Hotel name or Address",
        onStartTextChanged = onLodgingTextChanged,
        showEndTextField = false,
        endTitle = { Text("Check-out") },
        endTimeSelectorLabel = "Check-out Time",
        endLabelText = "Check-out time",
        endPlaceHolder = "Check-out time",
    )
}

@Composable
@Preview
fun AddLodgingListItemPreview() {
    AppTheme {
        Surface {
            AddLodgingListItem(
                uiState = AddLodgingItemState(
                    "", Time.now(),
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
                startEndAddPlanState = rememberStartEndAddPlanListItemState(
                    rememberAddPlanRowState(),
                    rememberAddPlanRowState()
                ),
                onLodgingTextChanged = {},
            )
        }
    }
}
