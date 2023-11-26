package com.combah.travel2.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
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
    ListItem(
        headlineContent = {
            Row {
                if (firstInDate) {
                    LeadingDate(
                        dayOfMonth = event.timestamp.dayOfMonthString(),
                        dayOfWeek = event.timestamp.dayOfWeekString(),
                        showSmall = false,
                    )
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
                    supportingContent = {
                        Text(
                            text = event.location,
                            style = typography.bodyMedium
                        )
                    }
                )
            }
        })
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