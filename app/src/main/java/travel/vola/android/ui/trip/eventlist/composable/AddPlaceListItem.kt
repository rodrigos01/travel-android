package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddPlaceItemState
import java.time.ZonedDateTime

@Composable
fun AddPlaceListItem(
    uiState: AddPlaceItemState,
    onTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        startDateTime: ZonedDateTime?,
        startTimeSelected: Boolean,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) -> Unit,
    minEndTime: ZonedDateTime? = null,
) {
    var selectedStartDateTime by remember {
        mutableStateOf(uiState.timestamp)
    }
    var startTimeSelected by remember {
        mutableStateOf(uiState.startTimeSelected)
    }
    var hasEnd by remember(uiState.endDateTime) {
        mutableStateOf(uiState.endDateTime != null)
    }
    var selectedEndDateTime by remember {
        mutableStateOf(uiState.endDateTime)
    }
    var endTimeSelected by remember {
        mutableStateOf(uiState.endTimeSelected)
    }
    var selectedSearchResultIndex by remember {
        mutableIntStateOf(-1)
    }
    LaunchedEffect(
        selectedStartDateTime,
        startTimeSelected,
        selectedEndDateTime,
        endTimeSelected,
        selectedSearchResultIndex,
    ) {
        onUpdated(
            selectedStartDateTime,
            startTimeSelected,
            selectedEndDateTime,
            endTimeSelected,
            selectedSearchResultIndex,
        )
    }
    Column {
        AddPlanRow(
            initialDateTime = selectedStartDateTime,
            timeSelectedInitially = startTimeSelected,
            title = if (hasEnd) {
                { Text("Start") }
            } else {
                {}
            },
            labelText = "Location",
            placeHolder = "Enter Location",
            text = uiState.placeName,
            onTextChanged = { onTextChanged(it.toString()) },
            searchResults = uiState.searchResults,
            timeSelectorLabel = "Pick Time",
            showTextField = true,
            onUpdated = { selectedDateTime, timeSelected, selectedIndex ->
                selectedStartDateTime =
                    selectedDateTime ?: error("Start date time should never be null")
                startTimeSelected = timeSelected
                selectedSearchResultIndex = selectedIndex
            },
        )
        if (hasEnd) {
            AddPlanRow(
                initialDateTime = selectedEndDateTime,
                timeSelectedInitially = endTimeSelected,
                title = { Text("End") },
                timeSelectorLabel = "Pick Time",
                minTime = minEndTime,
                onUpdated = { selectedDateTime, timeSelected, _ ->
                    selectedEndDateTime = selectedDateTime
                    endTimeSelected = timeSelected
                },
            )
        }
        TextButton(onClick = { hasEnd = !hasEnd }) {
            Text(if (!hasEnd) "Set end time" else "Remove end time")
        }
    }
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
                    startTimeSelected = true,
                    endTimeSelected = true,
                    endDateTime = ZonedDateTime.now(),
                    deleteButtonEnabled = true,
                    saveButtonEnabled = true,
                    typeSelectionEnabled = false,
                    dateSelectionEnabled = false,
                    minEndTime = null,
                    searchResults = emptyList(),
                    placeName = null,
                ),
                onTextChanged = {},
                onUpdated = { _, _, _, _, _ -> },
            )
        }
    }
}