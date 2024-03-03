package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import java.util.TimeZone

@Composable
fun AddLodgingListItem(
    onTypeSelected: (AddPlanType) -> Unit,
    minCheckInTime: Time,
    checkInDateSelectionEnabled: Boolean = false,
    checkInDayOfMonth: String,
    checkInDayOfWeek: String,
    onCheckInDateChanged: (Time) -> Unit,
    checkInTime: String? = null,
    onCheckInTimeChanged: (hour: Int, minute: Int) -> Unit,
    lodgingLabel: String? = null,
    onLodgingTextChanged: (CharSequence) -> Unit,
    lodgingSearchResults: List<String> = emptyList(),
    lodgingSearchResultTapped: (Int) -> Unit,
    checkOutDayOfMonth: String,
    checkOutDayOfWeek: String,
    minCheckOutTime: Time,
    onCheckOutDateChanged: (Time) -> Unit,
    checkOutTime: String? = null,
    onCheckOutTimeChanged: (hour: Int, minute: Int) -> Unit,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
) {
    StartEndAddPlanListItem(
        initialType = AddPlanType.Lodging,
        onTypeSelected,
        startTitle = { Text("CheckIn") },
        minStartTime = minCheckInTime,
        startDateSelectionEnabled = checkInDateSelectionEnabled,
        startTimeSelectorLabel = "Choose CheckIn Time",
        startDayOfMonth = checkInDayOfMonth,
        startDayOfWeek = checkInDayOfWeek,
        onStartDateChanged = onCheckInDateChanged,
        startTime = checkInTime,
        onStartTimeChanged = onCheckInTimeChanged,
        startLabelText = "Lodging Name",
        startPlaceHolder = "Enter Hotel name or Address",
        startText = lodgingLabel,
        onStartTextChanged = onLodgingTextChanged,
        startSearchResults = lodgingSearchResults,
        startSearchResultTapped = lodgingSearchResultTapped,
        showEndTextField = false,
        endTitle = { Text("Check-out") },
        minEndTime = minCheckOutTime,
        endDayOfMonth = checkOutDayOfMonth,
        endDayOfWeek = checkOutDayOfWeek,
        onEndDateChanged = onCheckOutDateChanged,
        endTimeSelectorLabel = "Choose CheckOut Time",
        endTime = checkOutTime,
        onEndTimeChanged = onCheckOutTimeChanged,
        endLabelText = "Check-out time",
        endPlaceHolder = "Check-out time",
        endText = checkOutTime,
        onCancelButtonTapped = onCancelButtonTapped,
        onSaveButtonTapped = onSaveButtonTapped,
    )
}

@Composable
@Preview
fun AddLodgingListItemPreview() {
    AppTheme {
        AddLodgingListItem(
            onTypeSelected = {},
            minCheckInTime = Time(0L, TimeZone.getDefault()),
            checkInDateSelectionEnabled = true,
            checkInDayOfMonth = "14",
            checkInDayOfWeek = "Tue",
            onCheckInDateChanged = {},
            onCheckInTimeChanged = { _, _ -> },
            onLodgingTextChanged = {},
            lodgingSearchResultTapped = {},
            checkOutDayOfMonth = "15",
            checkOutDayOfWeek = "Wed",
            onCheckOutDateChanged = {},
            checkOutTime = null,
            minCheckOutTime = Time(0L, TimeZone.getDefault()),
            onCheckOutTimeChanged = { _, _ -> },
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
        )
    }
}
