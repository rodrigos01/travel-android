package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.theme.AppTheme
import java.util.Date

@Composable
fun ArrivalEventListItem(event: ArrivalEvent, firstInDate: Boolean = false) {
    EventListItem(
        event = event,
        firstInDate = firstInDate,
        icon = R.drawable.ic_flight_land_black_24dp,
        title = stringResource(R.string.flight_arrival_tile)
    )
}

@Composable
@Preview
fun ArrivalEventListItemPreview() {
    AppTheme {
        ArrivalEventListItem(
            event = ArrivalEvent(
                Airport(name = "Charles de Gaule"),
                Date(),
                Place()
            )
        )
    }
}