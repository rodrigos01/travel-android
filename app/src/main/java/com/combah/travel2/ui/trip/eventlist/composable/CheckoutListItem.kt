package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.CheckoutEvent
import java.util.*

@Composable
fun CheckoutListItem(event: CheckoutEvent, firstInDate: Boolean = false) {
    EventListItem(
        event = event,
        firstInDate = firstInDate,
        icon = R.drawable.ic_hotel_black_24dp,
        title = stringResource(id = R.string.hotel_checkout_title)
    )
}

@Composable
@Preview
fun CheckoutListItemPreview() {
    MaterialTheme {
        CheckoutListItem(
            event = CheckoutEvent(
                Hotel(
                    name = "Hotel des Arms",
                    checkout = Date(),
                    place = Place()
                )
            )
        )
    }
}