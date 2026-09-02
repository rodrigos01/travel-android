package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import java.time.ZonedDateTime

@Composable
fun StartEndAddPlanListItem(
    uiState: ManualStartEndAddPlanState,
    startTitle: @Composable () -> Unit,
    startTimeSelectorLabel: String,
    startLabelText: String? = null,
    startPlaceHolder: String? = null,
    onStartTextChanged: (CharSequence) -> Unit,
    endTitle: @Composable () -> Unit,
    endTimeSelectorLabel: String,
    showEndTimePickerButton: Boolean = true,
    endLabelText: String? = null,
    endPlaceHolder: String? = null,
    onEndTextChanged: (CharSequence) -> Unit = {},
    onUpdated: (
        startDateTime: ZonedDateTime,
        startTimeSelected: Boolean,
        selectedStartResultId: String?,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedEndResultId: String?,
    ) -> Unit,
    requiresEnd: Boolean = true,
    canSetEnd: Boolean = true,
) {
    var selectedStartDateTime by remember(uiState.startState.dateTime) {
        mutableStateOf(uiState.startState.dateTime)
    }
    var startTimeSelected by remember {
        mutableStateOf(uiState.startState.isTimeSet)
    }
    var selectedStartResultId by remember {
        mutableStateOf<String?>(null)
    }
    var hasEnd by remember(requiresEnd, canSetEnd) {
        mutableStateOf(requiresEnd && canSetEnd)
    }
    var selectedEndDateTime by remember(uiState.startState.dateTime) {
        mutableStateOf(uiState.endState.dateTime)
    }
    var endTimeSelected by remember {
        mutableStateOf(uiState.endState.isTimeSet)
    }
    var selectedEndResultId by remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(
        selectedStartDateTime,
        startTimeSelected,
        selectedStartResultId,
        selectedEndDateTime,
        endTimeSelected,
        selectedEndResultId,
    ) {
        onUpdated(
            selectedStartDateTime ?: error("Start date time should never be null"),
            startTimeSelected,
            selectedStartResultId,
            selectedEndDateTime,
            endTimeSelected,
            selectedEndResultId,
        )
    }
    Column {
        AddPlanRow(
            initialDateTime = selectedStartDateTime,
            timeSelectedInitially = startTimeSelected,
            title = if (hasEnd) {
                startTitle
            } else {
                {}
            },
            labelText = startLabelText,
            placeHolder = startPlaceHolder,
            text = uiState.startState.locationText,
            onTextChanged = onStartTextChanged,
            searchResults = uiState.startState.searchResults,
            timeSelectorLabel = startTimeSelectorLabel,
            showTextField = true,
            onUpdated = { selectedDateTime, timeSelected, selectedResultId ->
                selectedStartDateTime = selectedDateTime
                startTimeSelected = timeSelected
                selectedStartResultId = selectedResultId
            },
        )
        if (hasEnd) {
            AddPlanRow(
                initialDateTime = selectedEndDateTime,
                timeSelectedInitially = endTimeSelected,
                minTime = uiState.endState.minDateTime,
                searchResults = uiState.endState.searchResults,
                title = endTitle,
                timeSelectorLabel = endTimeSelectorLabel,
                text = uiState.endState.locationText,
                dateSelectionEnabled = uiState.endState.dateSelectionEnabled,
                showTextField = showEndTimePickerButton,
                labelText = endLabelText,
                placeHolder = endPlaceHolder,
                onTextChanged = onEndTextChanged,
                onUpdated = { selectedDateTime, timeSelected, selectedResultId ->
                    selectedEndDateTime = selectedDateTime
                    endTimeSelected = timeSelected
                    selectedEndResultId = selectedResultId
                },
            )
        }
        if (!requiresEnd && canSetEnd) {
            TextButton(onClick = { hasEnd = !hasEnd }) {
                Text(if (!hasEnd) "Set end time" else "Remove end time")
            }
        }
    }
}

@Composable
@Preview
fun StartEndAddPlanListItemPreview() {
    AppTheme {
        Surface {
            StartEndAddPlanListItem(
                uiState = AddFlightItemState(
                    id = "",
                    timestamp = ZonedDateTime.now(),
                    typeSelectionEnabled = false,
                    startState = ManualAddPlanState(
                        dateTime = zonedDateTime("2025-09-21T14:49 +0100"),
                        minDateTime = ZonedDateTime.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    endState = ManualAddPlanState(
                        dateTime = null,
                        minDateTime = ZonedDateTime.now(),
                        dateSelectionEnabled = true,
                        locationText = "Somewhere",
                        searchResults = emptyList(),
                        isTimeSet = false,
                    ),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                ),
                startTitle = { Text(stringResource(R.string.label_start)) },
                startTimeSelectorLabel = stringResource(R.string.action_pick_start_time),
                startPlaceHolder = "Enter Start point",
                startLabelText = "Start",
                onStartTextChanged = {},
                endTitle = { Text(stringResource(R.string.label_end)) },
                endTimeSelectorLabel = stringResource(R.string.action_pick_end_time),
                endPlaceHolder = "Enter End point",
                endLabelText = "End",
                onEndTextChanged = {},
                requiresEnd = false,
                onUpdated = { _, _, _, _, _, _ -> },
            )
        }
    }
}
