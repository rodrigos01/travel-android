package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddPlaceListItem(
    uiState: AddPlaceItemState,
    onUpdated: (AddPlaceItemState) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        startTitle = { Text(stringResource(R.string.label_start)) },
        startTimeSelectorLabel = stringResource(R.string.action_pick_start_time),
        startLabelText = "Location",
        startPlaceHolder = "Enter Location",
        onStartTextChanged = { text ->
            onUpdated(
                uiState.copy(
                    startState = uiState.startState.copy(locationText = text.toString(), selectedResultId = null),
                ),
            )
        },
        endTitle = { Text(stringResource(R.string.label_end)) },
        endTimeSelectorLabel = stringResource(R.string.action_pick_end_time),
        showEndTimePickerButton = false,
        endLabelText = "Pick End Time",
        requiresEnd = false,
        onUpdated = { startDateTime, startTimeSelected, selectedStartResultId, endDateTime, endTimeSelected, _ ->
            onUpdated(
                uiState.copy(
                    timestamp = startDateTime,
                    startState = uiState.startState.copy(
                        dateTime = startDateTime,
                        isTimeSet = startTimeSelected,
                        selectedResultId = selectedStartResultId,
                    ),
                    endState = uiState.endState.copy(dateTime = endDateTime, isTimeSet = endTimeSelected),
                ),
            )
        },
    )
}

@PreviewLightDark
@Composable
fun AddPlaceListItemPreview() {
    AppTheme {
        Surface {
            AddPlaceListItem(
                uiState = AddPlaceItemState(
                    id = "",
                    timestamp = ZonedDateTime.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = ZonedDateTime.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                    ),
                    endState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = ZonedDateTime.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = true,
                        locationText = null,
                        searchResults = emptyList(),
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                onUpdated = {},
            )
        }
    }
}
