package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun FlightEventListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    destination: String,
    airportName: String,
    showSeparator: Boolean,
) {
    EventListItem(
        showDate,
        dayOfMonthString,
        dayOfWeekString,
        timeString,
        icon = R.drawable.ic_flight_takeoff_black_24dp,
        headline = stringResource(R.string.flight_event_tile, destination),
        supporting = airportName,
        showSeparator,
    )
}

@Composable
@Preview
fun FlightEventListItemPreview() {
    AppTheme {
        FlightEventListItem(
            showDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            destination = "Paris",
            airportName = "John F Kennedy",
            showSeparator = true,
        )
    }
}