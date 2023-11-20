package com.combah.travel2.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import java.util.Date

@Composable
fun EventListItem(
    event: TripEvent,
    firstInDate: Boolean = false,
    @DrawableRes icon: Int,
    title: String = event.name
) {
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
    ) {
        if (firstInDate) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            ) {
                Text(
                    text = event.timestamp.dayOfMonthString(),
                    style = typography.headlineSmall
                )
                Text(text = event.timestamp.dayOfWeekString(), style = typography.bodyMedium)
            }
        }
        val paddingStart = if (firstInDate) 0.dp else 48.dp
        ListItem(
            modifier = Modifier.padding(start = paddingStart),
            leadingContent = {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Event Icon",
                )
            },
            overlineContent = {
                Text(
                    text = event.timestamp.timeString(),
                    style = typography.bodyMedium,
                )
            },
            headlineContent = { Text(title, style = typography.titleMedium) },
            supportingContent = { Text(text = event.location, style = typography.bodyMedium) }
        )
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