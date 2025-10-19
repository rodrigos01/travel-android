package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@Composable
fun FlightEventListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    destination: String,
    airportName: String,
    position: EventListItemPosition = EventListItemPosition.MIDDLE,
) {
    EventListItem(
        showDate,
        dayOfMonthString,
        dayOfWeekString,
        timeString,
        icon = R.drawable.flight_takeoff_baseline_24,
        headline = stringResource(R.string.flight_event_tile, destination),
        supporting = airportName,
        position = position
    )
}

@Composable
@Preview
fun FlightEventListItemPreview() {
    AppTheme {
        FlightEventListItem(
            showDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            destination = "Paris",
            airportName = "John F Kennedy",
        )
    }
}