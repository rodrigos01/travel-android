package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vola.android.extensions.now
import com.vola.android.model.data.Time
import com.vola.android.ui.theme.AppTheme
import com.vola.android.ui.trip.state.ManualAddLodgingItemState
import com.vola.android.ui.trip.state.ManualAddPlanState
import com.vola.android.ui.trip.state.ManualStartEndAddPlanState

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
                uiState = ManualAddLodgingItemState(
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
