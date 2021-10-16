package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun TransportationSetup(
    departureDate: Date?,
    arrivalDate: Date?,
    onDepartureDateChanged: (Date) -> Unit,
    onArrivalDateChanged: (Date) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "")
                }
            },
            title = {
                Text(text = "Transportation")
            }
        )
    }) {
        var from by remember { mutableStateOf("") }
        var to by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text(text = "From") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(text = "To") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                DatePickerTextField(
                    label = "departure",
                    date = departureDate,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    onDateSelected = onDepartureDateChanged
                )
                DatePickerTextField(
                    label = "arrival",
                    date = arrivalDate,
                    minDate = departureDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = onArrivalDateChanged
                )
            }
        }
    }
}

@Composable
@Preview
fun TransportationSetupPreview() {
    MaterialTheme {
        TransportationSetup(Date(), Date(), {}, {})
    }
}