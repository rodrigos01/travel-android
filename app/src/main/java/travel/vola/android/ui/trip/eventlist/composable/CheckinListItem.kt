package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@Composable
fun CheckinListItem(
    showDate: Boolean = false,
    highlightDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    hotelName: String,
    position: EventListItemPosition = EventListItemPosition.MIDDLE,
) {
    EventListItem(
        showDate,
        highlightDate,
        dayOfMonthString,
        dayOfWeekString,
        timeString,
        icon = R.drawable.hotel_baseline_24,
        headline = stringResource(id = R.string.hotel_checkin_title),
        supporting = hotelName,
        position = position,
    )
}

@Composable
@Preview
fun CheckinListItemPreview() {
    AppTheme {
        CheckinListItem(
            showDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            hotelName = "Hotel des Arms",
        )
    }
}