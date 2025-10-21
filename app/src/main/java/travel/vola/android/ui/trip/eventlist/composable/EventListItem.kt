package travel.vola.android.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState

enum class EventListItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE,
}

@Composable
fun EventListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    @DrawableRes icon: Int,
    headline: String,
    supporting: String,
    position: EventListItemPosition = EventListItemPosition.MIDDLE,
) = EventListItem(
    showDate = showDate,
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
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    showTime: Boolean = true,
    iconPainter: Painter,
    headline: String,
    supporting: String,
    position: EventListItemPosition = EventListItemPosition.MIDDLE
) {
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        if (dayOfMonthString != null && dayOfWeekString != null) {
            Box(modifier = Modifier.alpha(if (showDate) 1F else 0F)) {
                LeadingDate(
                    dayOfMonth = dayOfMonthString,
                    dayOfWeek = dayOfWeekString,
                    showSmall = false,
                )
            }
        }
        val roundedCornerRadius = 12.dp
        val separatorPadding = 2.dp
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
                supportingColor = MaterialTheme.colorScheme.onSecondaryContainer,
                leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                overlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier
                .padding(
                    start = 8.dp,
                    top = if (position == EventListItemPosition.TOP || position == EventListItemPosition.SINGLE) 4.dp else separatorPadding,
                    bottom = if (position == EventListItemPosition.BOTTOM || position == EventListItemPosition.SINGLE) 4.dp else separatorPadding,
                )
                .clip(
                    RoundedCornerShape(
                        topStart = if (position == EventListItemPosition.TOP || position == EventListItemPosition.SINGLE) roundedCornerRadius else 0.dp,
                        topEnd = if (position == EventListItemPosition.TOP || position == EventListItemPosition.SINGLE) roundedCornerRadius else 0.dp,
                        bottomStart = if (position == EventListItemPosition.BOTTOM || position == EventListItemPosition.SINGLE) roundedCornerRadius else 0.dp,
                        bottomEnd = if (position == EventListItemPosition.BOTTOM || position == EventListItemPosition.SINGLE) roundedCornerRadius else 0.dp,
                    )
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

fun TripItemState.EventItemState.BackgroundStyle.asEvenListItemPosition() = when (this) {
    TripItemState.EventItemState.BackgroundStyle.TOP -> EventListItemPosition.TOP
    TripItemState.EventItemState.BackgroundStyle.MIDDLE -> EventListItemPosition.MIDDLE
    TripItemState.EventItemState.BackgroundStyle.BOTTOM -> EventListItemPosition.BOTTOM
    TripItemState.EventItemState.BackgroundStyle.SINGLE -> EventListItemPosition.SINGLE
}

@Composable
@PreviewLightDark
fun EventListItemPreview() {
    AppTheme {
        EventListItem(
            showDate = true,
            icon = R.drawable.flight_takeoff_baseline_24,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            headline = "Flight to Paris",
            supporting = "John F. Kennedy Intl.",
            position = EventListItemPosition.TOP,
        )
    }
}
