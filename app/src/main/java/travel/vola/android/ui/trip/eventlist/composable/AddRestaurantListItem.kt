package travel.vola.android.ui.trip.eventlist.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddRestaurantListItem(
    uiState: ManualStartEndAddPlanState,
    onTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        dateTime: ZonedDateTime?,
        timeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) -> Unit,
) {
    StartEndAddPlanListItem(
        uiState = uiState,
        startTitle = { Text(stringResource(R.string.label_start)) },
        startTimeSelectorLabel = stringResource(R.string.action_pick_time),
        startLabelText = "Location",
        startPlaceHolder = "Enter Location",
        onStartTextChanged = onTextChanged,
        endTitle = {},
        endTimeSelectorLabel = "",
        canSetEnd = false,
        onUpdated = {
                startDateTime,
                startTimeSelected,
                selectedStartSearchResultIndex,
                endDateTime,
                endTimeSelected,
                _,
            ->
            onUpdated(
                startDateTime,
                startTimeSelected,
                selectedStartSearchResultIndex
            )
        })
}

@PreviewLightDark
@Composable
fun AddRestaurantListItemPreview() {
    AppTheme {
        Surface {
            AddRestaurantListItem(
                uiState = ManualAddLodgingItemState(
                    "", ZonedDateTime.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = ZonedDateTime.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    endState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = ZonedDateTime.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = true,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                onTextChanged = {},
                onUpdated = { _, _, _ -> },
            )
        }
    }
}