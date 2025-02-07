package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import java.util.TimeZone

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddFlightListItem(
    onTypeSelected: (AddPlanType) -> Unit,
    minDepartureTime: Time,
    minArrivalTime: Time,
    departureDateSelectionEnabled: Boolean = false,
    departureDayOfMonth: String,
    departureDayOfWeek: String,
    onDepartureDateChanged: (Time) -> Unit,
    departureTime: String? = null,
    onDepartureTimeChanged: (hour: Int, minute: Int) -> Unit,
    airportFromName: String? = null,
    onAirportFromTextChanged: (CharSequence) -> Unit,
    airportFromSearchResults: List<String> = emptyList(),
    airportFromSearchResultTapped: (Int) -> Unit,
    arrivalTime: String? = null,
    arrivalDayOfMonth: String,
    arrivalDayOfWeek: String,
    onArrivalDateChanged: (Time) -> Unit,
    onArrivalTimeChanged: (hour: Int, minute: Int) -> Unit,
    airportToName: String? = null,
    onAirportToTextChanged: (CharSequence) -> Unit,
    airportToSearchResults: List<String> = emptyList(),
    airportToSearchResultTapped: (Int) -> Unit,
    saveButtonEnabled: Boolean,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
    isEditing: Boolean = false,
    onDeleteButtonTapped: () -> Unit = {}
) {
    StartEndAddPlanListItem(
        initialType = AddPlanType.Flight,
        onTypeSelected,
        typeSelectionEnabled = !isEditing,
        startTitle = { Text("Departure") },
        minStartTime = minDepartureTime,
        startDateSelectionEnabled = departureDateSelectionEnabled,
        startDayOfMonth = departureDayOfMonth,
        startDayOfWeek = departureDayOfWeek,
        onStartDateChanged = onDepartureDateChanged,
        startTimeSelectorLabel = "Choose Departure Time",
        startTime = departureTime,
        onStartTimeChanged = onDepartureTimeChanged,
        startLabelText = "from",
        startPlaceHolder = "Enter City or Airport",
        startText = airportFromName,
        onStartTextChanged = onAirportFromTextChanged,
        startSearchResults = airportFromSearchResults,
        startSearchResultTapped = airportFromSearchResultTapped,
        endTitle = { Text("Arrival") },
        minEndTime = minArrivalTime,
        endDayOfMonth = arrivalDayOfMonth,
        endDayOfWeek = arrivalDayOfWeek,
        onEndDateChanged = onArrivalDateChanged,
        endTimeSelectorLabel = "Choose Arrival Time",
        endTime = arrivalTime,
        onEndTimeChanged = onArrivalTimeChanged,
        endLabelText = "to",
        endPlaceHolder = "Enter City or Airport",
        endText = airportToName,
        onEndTextChanged = onAirportToTextChanged,
        endSearchResults = airportToSearchResults,
        endSearchResultTapped = airportToSearchResultTapped,
        saveButtonEnabled = saveButtonEnabled,
        onCancelButtonTapped = onCancelButtonTapped,
        onSaveButtonTapped = onSaveButtonTapped,
        deleteButtonEnabled = isEditing,
        onDeleteConfirmed = onDeleteButtonTapped,
    )
}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        AddFlightListItem(
            onTypeSelected = {},
            minDepartureTime = Time(0L, TimeZone.getDefault()),
            minArrivalTime = Time(0L, TimeZone.getDefault()),
            onDepartureTimeChanged = { _, _ -> },
            departureDateSelectionEnabled = true,
            departureDayOfMonth = "14",
            departureDayOfWeek = "Tue",
            onDepartureDateChanged = {},
            onAirportFromTextChanged = {},
            airportFromSearchResultTapped = {},
            arrivalDayOfMonth = "2",
            arrivalDayOfWeek = "Wed",
            onArrivalDateChanged = {},
            onArrivalTimeChanged = { _, _ -> },
            onAirportToTextChanged = {},
            airportToSearchResultTapped = {},
            saveButtonEnabled = true,
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
        )
    }
}
