package com.combah.travel2.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.extensions.dayOfMonthString
import com.combah.travel2.extensions.dayOfWeekString
import com.combah.travel2.extensions.timeString
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.TripEvent
import java.util.*

@Composable
fun EventListItem(
    event: TripEvent,
    firstInDate: Boolean = false,
    @DrawableRes icon: Int,
    title: String = event.name
) {
    val typography = MaterialTheme.typography
    Row {
        if (firstInDate) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            ) {
                Text(
                    text = event.timestamp.dayOfMonthString(),
                    style = typography.h5
                )
                Text(text = event.timestamp.dayOfWeekString(), style = typography.overline)
            }
        }
        val paddingStart = if (firstInDate) 16.dp else 64.dp
        Column(modifier = Modifier.padding(start = paddingStart, top = 16.dp)) {
            Text(
                text = event.timestamp.timeString(),
                style = typography.caption
            )
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Image(painter = painterResource(id = icon), contentDescription = "Event Icon")
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Text(title, style = typography.subtitle1)
                    Text(text = event.location, style = typography.caption)
                }
            }
        }
    }
}

@Composable
@Preview
fun EventListItemPreview() {
    MaterialTheme {
        EventListItem(
            event = TripEvent("Flight to Paris", "John F Kennedy", Date(), Place()),
            true,
            icon = R.drawable.ic_flight_takeoff_black_24dp
        )
    }
}