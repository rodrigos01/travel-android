package com.combah.travel2.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        Row {
            if (dayOfMonthString != null && dayOfWeekString != null) {
                Box(modifier = Modifier.alpha(if (showDate) 1F else 0F)) {
                    LeadingDate(
                        dayOfMonth = dayOfMonthString,
                        dayOfWeek = dayOfWeekString,
                        showSmall = false,
                    )
                }
            }
            ListItem(
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
                },
            )
        }
    }
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
            supporting = "John F. Kennedy Intl.",
        )
    }
}