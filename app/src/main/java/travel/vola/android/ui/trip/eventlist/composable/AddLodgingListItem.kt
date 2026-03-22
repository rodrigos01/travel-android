package travel.vola.android.ui.trip.eventlist.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

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
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddLodgingListItem(
    uiState: ManualStartEndAddPlanState,
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
            startTitle = { Text(stringResource(R.string.label_checkin)) },
            startTimeSelectorLabel = stringResource(R.string.action_pick_checkin_time),
            startLabelText = "Lodging Name",
            startPlaceHolder = "Enter Hotel name or Address",
            onStartTextChanged = onLodgingTextChanged,
            showEndTimePickerButton = false,
            endTitle = { Text(stringResource(R.string.label_checkout)) },
            endTimeSelectorLabel = stringResource(R.string.action_pick_checkout_time),
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
    }
}

@Composable
@Preview
fun AddLodgingListItemPreview() {
    AppTheme {
        Surface {
            AddLodgingListItem(
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
                onLodgingTextChanged = {},
                onFindLodgingButtonTapped = {},
                onUpdated = { _, _, _, _, _ -> },
            )
        }
    }
}
