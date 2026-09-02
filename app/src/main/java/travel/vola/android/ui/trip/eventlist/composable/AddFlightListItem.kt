package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddFlightListItem(
    uiState: AddFlightItemState,
    onUpdated: (AddFlightItemState) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        startTitle = { Text(stringResource(R.string.label_departure)) },
        startTimeSelectorLabel = stringResource(R.string.action_choose_departure_time),
        startLabelText = "from",
        startPlaceHolder = "Enter City or Airport",
        onStartTextChanged = { text ->
            onUpdated(
                uiState.copy(
                    startState = uiState.startState.copy(locationText = text.toString(), selectedResultId = null),
                ),
            )
        },
        endTitle = { Text(stringResource(R.string.label_arrival)) },
        endTimeSelectorLabel = stringResource(R.string.action_choose_arrival_time),
        endLabelText = "to",
        endPlaceHolder = "Enter City or Airport",
        onEndTextChanged = { text ->
            onUpdated(
                uiState.copy(
                    endState = uiState.endState.copy(locationText = text.toString(), selectedResultId = null),
                ),
            )
        },
        onUpdated = { startDateTime, startTimeSelected, selectedStartResultId, endDateTime, endTimeSelected, selectedEndResultId ->
            onUpdated(
                uiState.copy(
                    timestamp = startDateTime,
                    startState = uiState.startState.copy(
                        dateTime = startDateTime,
                        isTimeSet = startTimeSelected,
                        selectedResultId = selectedStartResultId,
                    ),
                    endState = uiState.endState.copy(
                        dateTime = endDateTime,
                        isTimeSet = endTimeSelected,
                        selectedResultId = selectedEndResultId,
                    ),
                ),
            )
        },
    )
}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        Surface {
            AddFlightListItem(
                uiState = AddFlightItemState(
                    "",
                    ZonedDateTime.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = zonedDateTime("2025-06-12T05:00 -0300"),
                        minDateTime = ZonedDateTime.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    endState = ManualAddPlanState(
                        dateTime = zonedDateTime("2025-06-12T05:00 -0300"),
                        minDateTime = ZonedDateTime.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                onUpdated = {},
            )
        }
    }
}
