package travel.vola.android.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

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
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
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
@PreviewLightDark
fun EventListItemPreview() {
    AppTheme {
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
