package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddRestaurantItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddRestaurantListItem(
    uiState: AddRestaurantItemState,
    onUpdated: (AddRestaurantItemState) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        startTitle = { Text(stringResource(R.string.label_start)) },
        startTimeSelectorLabel = stringResource(R.string.action_pick_time),
        startLabelText = "Location",
        startPlaceHolder = "Enter Location",
        onStartTextChanged = { text ->
            onUpdated(
                uiState.copy(
                    startState = uiState.startState.copy(locationText = text.toString(), selectedResultId = null),
                ),
            )
        },
        endTitle = {},
        endTimeSelectorLabel = "",
        canSetEnd = false,
        onUpdated = { startDateTime, startTimeSelected, selectedStartResultId, _, _, _ ->
            onUpdated(
                uiState.copy(
                    timestamp = startDateTime,
                    startState = uiState.startState.copy(
                        dateTime = startDateTime,
                        isTimeSet = startTimeSelected,
                        selectedResultId = selectedStartResultId,
                    ),
                ),
            )
        },
    )
}

@PreviewLightDark
@Composable
fun AddRestaurantListItemPreview() {
    AppTheme {
        Surface {
            AddRestaurantListItem(
                uiState = AddRestaurantItemState(
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
