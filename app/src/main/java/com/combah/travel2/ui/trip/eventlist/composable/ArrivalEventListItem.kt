package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.ArrivalEvent
import java.util.*

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
    MaterialTheme {
        ArrivalEventListItem(
            event = ArrivalEvent(
                Airport(name = "Charles de Gaule"),
                Date(),
                Place()
            )
        )
    }
}