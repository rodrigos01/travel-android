package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import java.util.TimeZone

@Composable
fun StartEndAddPlanListItem(
    initialType: AddPlanType,
    onTypeSelected: (AddPlanType) -> Unit,
    typeSelectionEnabled: Boolean,
    startTitle: @Composable () -> Unit,
    minStartTime: Time,
    startDateSelectionEnabled: Boolean = true,
    startDayOfMonth: String,
    startDayOfWeek: String,
    onStartDateChanged: (Time) -> Unit,
    startTimeSelectorLabel: String,
    startTime: String? = null,
    onStartTimeChanged: (hour: Int, minute: Int) -> Unit,
    startLabelText: String? = null,
    startPlaceHolder: String? = null,
    startText: String? = null,
    onStartTextChanged: (CharSequence) -> Unit,
    startSearchResults: List<String> = emptyList(),
    startSearchResultTapped: (Int) -> Unit,
    endTitle: @Composable () -> Unit,
    minEndTime: Time,
    endTimeSelectorLabel: String,
    endTime: String? = null,
    endDayOfMonth: String,
    endDayOfWeek: String,
    onEndDateChanged: (Time) -> Unit,
    onEndTimeChanged: (hour: Int, minute: Int) -> Unit,
    showEndTextField: Boolean = true,
    endLabelText: String? = null,
    endPlaceHolder: String? = null,
    endText: String? = null,
    onEndTextChanged: (CharSequence) -> Unit = {},
    endSearchResults: List<String> = emptyList(),
    endSearchResultTapped: (Int) -> Unit = {},
    saveButtonEnabled: Boolean = true,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
    deleteButtonEnabled: Boolean,
    onDeleteConfirmed: () -> Unit,
) {
    AddPlanListItem(
        initialType,
        onTypeSelected,
        typeSelectionEnabled,
        deleteButtonEnabled,
        onDeleteConfirmed,
        primaryButtonEnabled = saveButtonEnabled,
        primaryButtonLabel = "Save",
        onPrimaryButtonTapped = onSaveButtonTapped,
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonTapped = onCancelButtonTapped,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        AddPlanRow(
            title = startTitle,
            minTime = minStartTime,
            timeSelectorLabel = startTimeSelectorLabel,
            time = startTime,
            onTimeChanged = onStartTimeChanged,
            dateSelectionEnabled = startDateSelectionEnabled,
            dayOfMonth = startDayOfMonth,
            dayOfWeek = startDayOfWeek,
            onDateChanged = onStartDateChanged,
            placeHolder = startPlaceHolder,
            labelText = startLabelText,
            text = startText,
            onTextChanged = onStartTextChanged,
            searchResults = startSearchResults,
            searchResultTapped = startSearchResultTapped,
        )
        AddPlanRow(
            title = endTitle,
            minTime = minEndTime,
            timeSelectorLabel = endTimeSelectorLabel,
            time = endTime,
            onTimeChanged = onEndTimeChanged,
            dayOfMonth = endDayOfMonth,
            dayOfWeek = endDayOfWeek,
            onDateChanged = onEndDateChanged,
            showTextField = showEndTextField,
            placeHolder = endPlaceHolder,
            labelText = endLabelText,
            text = endText,
            onTextChanged = onEndTextChanged,
            searchResults = endSearchResults,
            searchResultTapped = endSearchResultTapped,
        )
    }

}

@Composable
@Preview
fun StartEndAddPlanListItemPreview() {
    AppTheme {
        StartEndAddPlanListItem(
            initialType = AddPlanType.Flight,
            typeSelectionEnabled = false,
            onTypeSelected = {},
            startTitle = { Text("Start") },
            minStartTime = Time(0L, TimeZone.getDefault()),
            startTimeSelectorLabel = "Pick Start Time",
            onStartTimeChanged = { _, _ -> },
            startDateSelectionEnabled = false,
            startDayOfMonth = "14",
            startDayOfWeek = "Tue",
            onStartDateChanged = {},
            startPlaceHolder = "Enter Start point",
            startLabelText = "Start",
            onStartTextChanged = {},
            startSearchResultTapped = {},
            endTitle = { Text("End") },
            minEndTime = Time(0L, TimeZone.getDefault()),
            endTimeSelectorLabel = "Pick End Time",
            endDayOfMonth = "2",
            endDayOfWeek = "Wed",
            onEndDateChanged = {},
            endPlaceHolder = "Enter End point",
            endLabelText = "End",
            onEndTimeChanged = { _, _ -> },
            onEndTextChanged = {},
            endSearchResultTapped = {},
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
            deleteButtonEnabled = true,
            onDeleteConfirmed = {},
        )
    }
}
