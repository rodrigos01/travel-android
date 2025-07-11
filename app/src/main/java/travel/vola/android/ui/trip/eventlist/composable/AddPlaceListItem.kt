package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddPlaceListItem(
    uiState: ManualStartEndAddPlanState,
    onTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        startDateTime: ZonedDateTime?,
        startTimeSelected: Boolean,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) -> Unit,
) {
    StartEndAddPlanListItem(uiState = uiState,
        startTitle = { Text("Start") },
        startTimeSelectorLabel = "Pick Start Time",
        startLabelText = "Location",
        startPlaceHolder = "Enter Location",
        onStartTextChanged = onTextChanged,
        endTitle = { Text("End") },
        endTimeSelectorLabel = "Pick End Time",
        showEndTimePickerButton = false,
        endLabelText = "Pick End Time",
        requiresEnd = false,
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
                endDateTime,
                endTimeSelected,
                selectedStartSearchResultIndex
            )
        })
}

@PreviewLightDark
@Composable
fun AddPlaceListItemPreview() {
    AppTheme {
        Surface {
            AddPlaceListItem(
                uiState = ManualAddLodgingItemState(
                    "", Time.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = Time.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    endState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = Time.now(),
                        isTimeSet = false,
                        dateSelectionEnabled = true,
                        locationText = null,
                        searchResults = emptyList()
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                onTextChanged = {},
                onUpdated = { _, _, _, _, _ -> },
            )
        }
    }
}