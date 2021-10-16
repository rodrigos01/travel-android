package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.CheckinEvent
import java.util.*

@Composable
fun CheckinListItem(event: CheckinEvent, firstInDate: Boolean = false) {
    EventListItem(
        event = event,
        firstInDate = firstInDate,
        icon = R.drawable.ic_hotel_black_24dp,
        title = stringResource(id = R.string.hotel_checkin_title)
    )
}

@Composable
@Preview
fun CheckinListItemPreview() {
    MaterialTheme {
        CheckinListItem(
            event = CheckinEvent(
                Hotel(
                    name = "Hotel des Arms",
                    checkin = Date(),
                    place = Place()
                )
            )
        )
    }
}