package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun AddFlightListItem(
    departureTime: String,
    airportFromName: String? = null,
    airportFromSearchResults: List<String> = emptyList(),
    arrivalTime: String,
    arrivalDayOfMonth: String,
    arrivalDayOfWeek: String,
    airportToName: String? = null,
    airportToSearchResults: List<String> = emptyList(),
) {

}

@Composable
@Preview
fun AddFlightListItemPreview() {
    AppTheme {
        AddFlightListItem(
            departureTime = "6:15 PM",
            arrivalTime = "set arrival time",
            arrivalDayOfMonth = "15",
            arrivalDayOfWeek = "Wed",
        )
    }
}