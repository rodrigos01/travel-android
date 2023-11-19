package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.FlightEvent
import com.combah.travel2.ui.theme.AppTheme
import java.util.Date

@Composable
fun FlightEventListItem(event: FlightEvent, firstInDate: Boolean = false) {
    EventListItem(
        event = event,
        firstInDate = firstInDate,
        icon = R.drawable.ic_flight_takeoff_black_24dp,
        title = stringResource(R.string.flight_event_tile, event.destination.name)
    )
}

@Composable
@Preview
fun FlightEventListItemPreview() {
    AppTheme {
        FlightEventListItem(
            event = FlightEvent(
                Place(name = "New York"),
                Place(name = "Paris"),
                Airport(name = "John F Kennedy"),
                Date()
            )
        )
    }
}