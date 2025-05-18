package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.Time
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun AddFlightListItem(
    uiState: ManualStartEndAddPlanState,
    startEndAddPlanState: StartEndAddPlanListItemState,
    onAirportFromTextChanged: (CharSequence) -> Unit,
    onAirportToTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        departureDateTime: ZonedDateTime,
        departureTimeSelected: Boolean,
        selectedDepartureSearchResultIndex: Int,
        arrivalDateTime: ZonedDateTime?,
        arrivalTimeSelected: Boolean,
        selectedArrivalSearchResultIndex: Int,
    ) -> Unit,
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
        onUpdated = onUpdated,
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
                        dateTime = Time("2025-06-12T05:00 -0300"),
                        minDateTime = Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    endState = ManualAddPlanState(
                        dateTime = Time("2025-06-12T05:00 -0300"),
                        minDateTime = Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                startEndAddPlanState = rememberStartEndAddPlanListItemState(
                    rememberAddPlanRowState(), rememberAddPlanRowState()
                ),
                onAirportFromTextChanged = {},
                onAirportToTextChanged = {},
                onUpdated = { _, _, _, _, _, _ -> },
            )
        }
    }
}
