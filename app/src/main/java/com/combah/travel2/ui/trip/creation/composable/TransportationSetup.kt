package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.extensions.moveFocus
import com.combah.travel2.ui.trip.creation.TransportationSetupViewModel

@Composable
fun TransportationSetup(
    viewModel: TransportationSetupViewModel
) {
    Scaffold(
        topBar = {
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
        }) { paddingValues ->
        var from by remember { mutableStateOf("") }
        var to by remember { mutableStateOf("") }
        val departureDate by viewModel.departureDate.observeAsState()
        val arrivalDate by viewModel.arrivalDate.observeAsState()
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text(text = "From") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    keyboardActions = moveFocus(FocusDirection.Right),
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(text = "To") },
                    modifier = Modifier.weight(1f),
                    keyboardActions = moveFocus(FocusDirection.Down),
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                DatePickerTextField(
                    label = "departure",
                    date = departureDate,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    onDateSelected = {
                        viewModel.departureDate.value = it
                    }
                )
                DatePickerTextField(
                    label = "arrival",
                    date = arrivalDate,
                    minDate = departureDate,
                    modifier = Modifier.weight(1f),
                    onDateSelected = {
                        viewModel.arrivalDate.value = it
                    }
                )
            }
        }
    }
}

@Composable
@Preview
fun TransportationSetupPreview() {
    MaterialTheme {
        TransportationSetup(TransportationSetupViewModel())
    }
}

object TransportationSetupDestination {
    const val KEY = "transportation_setup"
}