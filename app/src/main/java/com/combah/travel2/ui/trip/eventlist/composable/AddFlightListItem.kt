package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.AutoCompleteTextField
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton
import com.combah.travel2.ui.trip.creation.composable.TimePickerTextButton
import com.combah.travel2.ui.trip.creation.composable.TypeSelectorButton
import com.combah.travel2.ui.trip.creation.composable.rememberAutoCompleteTextFieldState
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
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
) {
    ConstraintLayout(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
    ) {
        val (
            departureLabel,
            departureDaySelector,
            departureTimeSelector,
            typeSelector,
            airportFrom,
            arrivalLabel,
            arrivalTimeSelector,
            arrivalDaySelector,
            airportTo,
            cancelButton,
            saveButton,
        ) = createRefs()
        TypeSelectorButton(
            initialType = AddPlanType.Flight,
            onOptionSelected = onTypeSelected,
            modifier = Modifier
                .constrainAs(typeSelector) {
                    top.linkTo(parent.top, margin = 8.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                },
        )
        Text(text = "Departure",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(departureLabel) {
                top.linkTo(typeSelector.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        TimePickerTextButton(
            text = departureTime ?: "Choose Departure Time",
            onDepartureTimeChanged,
            modifier = Modifier
                .semantics { role = Role.Button }
                .constrainAs(departureTimeSelector) {
                    top.linkTo(departureLabel.top)
                    bottom.linkTo(departureLabel.bottom)
                    start.linkTo(airportFrom.start)
                },
        )
        if (departureDateSelectionEnabled) {
            DatePickerButton(
                minDepartureTime,
                departureDayOfMonth,
                departureDayOfWeek,
                onDepartureDateChanged,
                modifier = Modifier
                    .constrainAs(departureDaySelector) {
                        top.linkTo(airportFrom.top)
                        bottom.linkTo(airportFrom.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
            )
        } else {
            LeadingDate(
                departureDayOfMonth, departureDayOfWeek,
                modifier = Modifier
                    .constrainAs(departureDaySelector) {
                        top.linkTo(airportFrom.top)
                        bottom.linkTo(airportFrom.bottom)
                        start.linkTo(parent.start, margin = 16.dp)
                        end.linkTo(arrivalDaySelector.end)
                        width = Dimension.fillToConstraints
                    },
            )
        }
        AutoCompleteTextField(
            state = rememberAutoCompleteTextFieldState(
                airportFromName, airportFromSearchResults,
            ),
            label = "from",
            placeHolder = "Enter City or Airport",
            onAirportFromTextChanged,
            airportFromSearchResultTapped,
            modifier = Modifier
                .constrainAs(airportFrom) {
                    start.linkTo(departureDaySelector.end, margin = 8.dp)
                    top.linkTo(departureTimeSelector.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                },
        )
        Text(text = "Arrival",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(arrivalLabel) {
                top.linkTo(departureDaySelector.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        TimePickerTextButton(text = arrivalTime ?: "Choose Arrival Time",
            onArrivalTimeChanged,
            modifier = Modifier.constrainAs(arrivalTimeSelector) {
                top.linkTo(arrivalLabel.top)
                bottom.linkTo(arrivalLabel.bottom)
                start.linkTo(airportTo.start)
            })
        DatePickerButton(
            minArrivalTime,
            arrivalDayOfMonth,
            arrivalDayOfWeek,
            onArrivalDateChanged,
            modifier = Modifier
                .constrainAs(arrivalDaySelector) {
                    top.linkTo(airportTo.top, margin = 8.dp)
                    bottom.linkTo(airportTo.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                },
        )
        AutoCompleteTextField(
            state = rememberAutoCompleteTextFieldState(
                airportToName,
                airportToSearchResults,
            ),
            label = "To",
            placeHolder = "Enter City or Airport",
            onAirportToTextChanged,
            airportToSearchResultTapped,
            modifier = Modifier.constrainAs(airportTo) {
                start.linkTo(arrivalDaySelector.end, margin = 8.dp)
                top.linkTo(arrivalTimeSelector.bottom)
                end.linkTo(parent.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            })
        OutlinedButton(onClick = { onCancelButtonTapped() },
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                end.linkTo(saveButton.start, margin = 8.dp)
            }) {
            Text("Cancel")
        }
        Button(onClick = { onSaveButtonTapped() }, modifier = Modifier.constrainAs(saveButton) {
            top.linkTo(arrivalDaySelector.bottom, margin = 8.dp)
            bottom.linkTo(parent.bottom, margin = 16.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }) {
            Text("Save")
        }
    }

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
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
        )
    }
}
