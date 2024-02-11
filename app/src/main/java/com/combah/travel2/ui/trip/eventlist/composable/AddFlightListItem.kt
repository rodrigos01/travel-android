package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.combah.travel2.R
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import com.combah.travel2.ui.trip.creation.composable.selectedTime

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddFlightListItem(
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
    airportToName: String? = null,
    onAirportToTextChanged: (CharSequence) -> Unit,
    airportToSearchResults: List<String> = emptyList(),
    airportToSearchResultTapped: (Int) -> Unit,
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
        val showDepartureTimePicker = remember { mutableStateOf(false) }
        val departureTimePickerState = rememberTimePickerState()
        Surface(
            onClick = { showDepartureTimePicker.value = true },
            modifier = Modifier
                .semantics { role = Role.Button }
                .constrainAs(departureTimeSelector) {
                    top.linkTo(departureLabel.bottom)
                    start.linkTo(airportFrom.start)
                },
        ) {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_time_16),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 4.dp)
                )
                Text(
                    text = departureTime ?: "Choose Departure Time",
                    style = TextStyle(color = MaterialTheme.colorScheme.tertiary)
                )
            }
        }
        if (showDepartureTimePicker.value) {
            ConfirmationDialog(
                onConfirm = {
                    onDepartureTimeChanged(
                        departureTimePickerState.hour,
                        departureTimePickerState.minute,
                    )
                    showDepartureTimePicker.value = false
                },
                onDismiss = { showDepartureTimePicker.value = false },
                buttonEnabled = true,
            ) {
                TimePicker(
                    modifier = Modifier.padding(top = 16.dp), state = departureTimePickerState,
                )
            }
        }
        OutlinedButton(onClick = { /*TODO*/ },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .constrainAs(typeSelector) {
                    top.linkTo(airportFrom.top, margin = 8.dp)
                    bottom.linkTo(airportFrom.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                    height = Dimension.fillToConstraints
                }
                .wrapContentWidth()) {
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
        Box(modifier = Modifier
            .constrainAs(airportFrom) {
                start.linkTo(typeSelector.end, margin = 8.dp)
                top.linkTo(departureTimeSelector.bottom, margin = 8.dp)
                end.linkTo(parent.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            }
            .wrapContentSize(Alignment.TopStart)) {
            OutlinedTextField(
                value = airportFromName.orEmpty(),
                label = {
                    Text("from")
                },
                placeholder = {
                    Text("Enter City or Airport")
                },
                onValueChange = onAirportFromTextChanged,
            )
            if (airportFromSearchResults.isNotEmpty()) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { onAirportFromTextChanged("") },
                ) {
                    airportFromSearchResults.forEachIndexed { index, airportName ->
                        DropdownMenuItem(
                            text = { Text(airportName) },
                            onClick = { airportFromSearchResultTapped(index) },
                            colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        )
                    }
                }
            }
        }
        Text(text = "Arrival",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(arrivalLabel) {
                top.linkTo(typeSelector.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        Row(modifier = Modifier.constrainAs(arrivalTimeSelector) {
            top.linkTo(arrivalLabel.bottom)
            start.linkTo(airportTo.start)
        }) {
            Image(
                painter = painterResource(id = R.drawable.ic_time_16),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
            )
            Text(
                text = arrivalTime ?: "Choose Arrival Time",
                style = TextStyle(color = MaterialTheme.colorScheme.tertiary)
            )
        }
        val showArrivalDatePicker = remember { mutableStateOf(false) }
        val arrivalDatePickerState = rememberDatePickerState()
        FilledTonalButton(onClick = { showArrivalDatePicker.value = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .constrainAs(arrivalDaySelector) {
                    top.linkTo(airportTo.top, margin = 8.dp)
                    bottom.linkTo(airportTo.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                }
                .wrapContentWidth()
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
//                    dateValidator = { validateTime(state.minDate?.midnightTime?.minus(1), it) },
                )
            }
        }
        Box(modifier = Modifier.constrainAs(airportTo) {
            start.linkTo(arrivalDaySelector.end, margin = 8.dp)
            top.linkTo(arrivalTimeSelector.bottom, margin = 8.dp)
            end.linkTo(parent.end, margin = 16.dp)
            bottom.linkTo(parent.bottom, margin = 16.dp)
            width = Dimension.fillToConstraints
        }) {
            OutlinedTextField(
                value = airportToName.orEmpty(),
                label = {
                    Text("to")
                },
                placeholder = {
                    Text("Enter City or Airport")
                },
                onValueChange = onAirportToTextChanged,
            )
            if (airportToSearchResults.isNotEmpty()) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { onAirportToTextChanged("") },
                ) {
                    airportToSearchResults.forEachIndexed { index, airportName ->
                        DropdownMenuItem(text = { Text(airportName) },
                            onClick = { airportToSearchResultTapped(index) })
                    }
                }
            }
        }
    }

}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        AddFlightListItem(
            onDepartureTimeChanged = { _, _ -> },
            onAirportFromTextChanged = {},
            airportFromSearchResultTapped = {},
            arrivalDayOfMonth = "15",
            arrivalDayOfWeek = "Wed",
            onArrivalDateChanged = {},
            onAirportToTextChanged = {},
            airportToSearchResultTapped = {},
        )
    }
}