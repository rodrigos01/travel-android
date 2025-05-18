package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddLodgingListItem(
    uiState: ManualStartEndAddPlanState,
    startEndAddPlanState: StartEndAddPlanListItemState,
    onLodgingTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        checkIn: ZonedDateTime,
        checkInTimeSelected: Boolean,
        checkOut: ZonedDateTime?,
        checkOutTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) -> Unit,
    onFindLodgingButtonTapped: () -> Unit,
) {
    Column {
        StartEndAddPlanListItem(
            uiState = uiState,
            state = startEndAddPlanState,
            startTitle = { Text("CheckIn") },
            startTimeSelectorLabel = "Check-in Time",
            startLabelText = "Lodging Name",
            startPlaceHolder = "Enter Hotel name or Address",
            onStartTextChanged = onLodgingTextChanged,
            showEndTimePickerButton = false,
            endTitle = { Text("Check-out") },
            endTimeSelectorLabel = "Check-out Time",
            endLabelText = "Check-out time",
            endPlaceHolder = "Check-out time",
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
            },
        )
        TextButton(
            onClick = onFindLodgingButtonTapped,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Find lodging")
        }
    }
}

@Composable
@Preview
fun AddLodgingListItemPreview() {
    AppTheme {
        Surface {
            AddLodgingListItem(
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
                startEndAddPlanState = rememberStartEndAddPlanListItemState(
                    rememberAddPlanRowState(), rememberAddPlanRowState()
                ),
                onLodgingTextChanged = {},
                onFindLodgingButtonTapped = {},
                onUpdated = { _, _, _, _, _ -> },
            )
        }
    }
}
