package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.combah.travel2.R
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AutoCompleteTextField
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import com.combah.travel2.ui.trip.creation.composable.TimePickerTextButton
import com.combah.travel2.ui.trip.creation.composable.rememberAutoCompleteTextFieldState
import com.combah.travel2.ui.trip.creation.composable.selectedTime

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddFlightListItem(
    minArrivalTimeMillis: Long,
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
        Text(text = "Departure",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(departureLabel) {
                top.linkTo(parent.top, margin = 16.dp)
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
        OutlinedButton(
            onClick = { /*TODO*/ },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .constrainAs(typeSelector) {
                    top.linkTo(airportFrom.top, margin = 8.dp)
                    bottom.linkTo(airportFrom.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                    height = Dimension.fillToConstraints
                }
                .width(96.dp)) {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_flight_24dp),
                    contentDescription = null,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        AutoCompleteTextField(
            state = rememberAutoCompleteTextFieldState(
                airportFromName, airportFromSearchResults,
            ),
            onAirportFromTextChanged,
            airportFromSearchResultTapped,
            modifier = Modifier
                .constrainAs(airportFrom) {
                    start.linkTo(typeSelector.end, margin = 8.dp)
                    top.linkTo(departureTimeSelector.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                }
                .wrapContentSize(Alignment.TopStart)
        )
        Text(text = "Arrival",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(arrivalLabel) {
                top.linkTo(typeSelector.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        TimePickerTextButton(
            text = arrivalTime ?: "Choose Arrival Time",
            onArrivalTimeChanged,
            modifier = Modifier.constrainAs(arrivalTimeSelector) {
                top.linkTo(arrivalLabel.top)
                bottom.linkTo(arrivalLabel.bottom)
                start.linkTo(airportTo.start)
            }
        )
        val showArrivalDatePicker = remember { mutableStateOf(false) }
        val arrivalDatePickerState = rememberDatePickerState(
            initialDisplayedMonthMillis = minArrivalTimeMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis > minArrivalTimeMillis
                }
            }
        )
        FilledTonalButton(
            onClick = { showArrivalDatePicker.value = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .constrainAs(arrivalDaySelector) {
                    top.linkTo(airportTo.top, margin = 8.dp)
                    bottom.linkTo(airportTo.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                }
                .width(96.dp)
                .wrapContentHeight()) {
            Row {
                LeadingDate(dayOfMonth = arrivalDayOfMonth, dayOfWeek = arrivalDayOfWeek)
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        if (showArrivalDatePicker.value) {
            ConfirmationDialog(
                onConfirm = {
                    arrivalDatePickerState.selectedTime?.let {
                        onArrivalDateChanged(
                            it
                        )
                    }
                    showArrivalDatePicker.value = false
                },
                onDismiss = { showArrivalDatePicker.value = false },
                buttonEnabled = true,
            ) {
                DatePicker(
                    state = arrivalDatePickerState,
                )
            }
        }
        AutoCompleteTextField(
            state = rememberAutoCompleteTextFieldState(
                airportToName,
                airportToSearchResults,
            ),
            onAirportToTextChanged,
            airportToSearchResultTapped,
            modifier = Modifier.constrainAs(airportTo) {
                start.linkTo(arrivalDaySelector.end, margin = 8.dp)
                top.linkTo(arrivalTimeSelector.bottom)
                end.linkTo(parent.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            }
        )
        OutlinedButton(
            onClick = { onSaveButtonTapped() },
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                end.linkTo(saveButton.start, margin = 8.dp)
            }
        ) {
            Text("Cancel")
        }
        Button(
            onClick = { onCancelButtonTapped() },
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(arrivalDaySelector.bottom, margin = 8.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
            }
        ) {
            Text("Save")
        }
    }

}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        AddFlightListItem(
            minArrivalTimeMillis = 0L,
            onDepartureTimeChanged = { _, _ -> },
            onAirportFromTextChanged = {},
            airportFromSearchResultTapped = {},
            arrivalDayOfMonth = "15",
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
