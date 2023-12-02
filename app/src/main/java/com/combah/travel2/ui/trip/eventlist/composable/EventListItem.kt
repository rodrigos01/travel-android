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

@Composable
fun EventListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    @DrawableRes icon: Int,
    headline: String,
    supporting: String,
) {
    val typography = MaterialTheme.typography
    ListItem(
        headlineContent = {
            Row {
                if (showDate && dayOfMonthString != null && dayOfWeekString != null) {
                    LeadingDate(
                        dayOfMonth = dayOfMonthString,
                        dayOfWeek = dayOfWeekString,
                        showSmall = false,
                    )
                }
                val paddingStart = if (showDate) 0.dp else 48.dp
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
                            text = timeString,
                            style = typography.bodyMedium,
                        )
                    },
                    headlineContent = { Text(headline, style = typography.titleMedium) },
                    supportingContent = {
                        Text(
                            text = supporting,
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
            showDate = true,
            icon = R.drawable.ic_flight_takeoff_black_24dp,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            headline = "Flight to Paris",
            supporting = "John F. Kennedy Intl."
        )
    }
}