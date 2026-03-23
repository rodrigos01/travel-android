package travel.vola.android.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@Composable
fun EventListItem(
    showDate: Boolean = false,
    highlightDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    @DrawableRes icon: Int,
    headline: String,
    supporting: String,
    position: EventItemPosition = EventItemPosition.MIDDLE,
) = EventListItem(
    showDate = showDate,
    highlightDate = highlightDate,
    dayOfMonthString = dayOfMonthString,
    dayOfWeekString = dayOfWeekString,
    timeString = timeString,
    iconPainter = painterResource(id = icon),
    headline = headline,
    supporting = supporting,
    position = position,
)

@Composable
fun EventListItem(
    showDate: Boolean = false,
    highlightDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    showTime: Boolean = true,
    iconPainter: Painter,
    headline: String,
    supporting: String,
    position: EventItemPosition = EventItemPosition.MIDDLE,
) {
    val typography = MaterialTheme.typography
    EventItem(
        showDate,
        highlightDate,
        dayOfMonthString,
        dayOfWeekString,
        position,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = LocalContentColor.current,
                supportingColor = LocalContentColor.current,
                leadingIconColor = LocalContentColor.current,
                overlineColor = LocalContentColor.current,
            ),
            leadingContent = {
                Icon(
                    painter = iconPainter,
                    tint = LocalContentColor.current,
                    contentDescription = "Event Icon",
                )
            },
            overlineContent = {
                if (showTime) {
                    Text(
                        text = timeString,
                        style = typography.bodyMedium,
                    )
                }
            },
            headlineContent = {
                Text(
                    headline,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            supportingContent = {
                Text(
                    text = supporting,
                    style = typography.bodyMedium,
                )
            },
        )
    }
}

@Composable
@PreviewLightDark
fun EventListItemPreview() {
    AppTheme {
        EventListItem(
            showDate = true,
            highlightDate = true,
            icon = R.drawable.flight_takeoff_baseline_24,
            dayOfMonthString = "21",
            dayOfWeekString = "Wed",
            timeString = "6:15 AM",
            headline = "Flight to Paris",
            supporting = "John F. Kennedy Intl.",
            position = EventItemPosition.SINGLE,
        )
    }
}
