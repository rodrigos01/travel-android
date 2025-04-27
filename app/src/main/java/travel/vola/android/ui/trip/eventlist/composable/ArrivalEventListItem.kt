package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@Composable
fun ArrivalEventListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    airportName: String,
) {
    EventListItem(
        showDate,
        dayOfMonthString,
        dayOfWeekString,
        timeString,
        icon = R.drawable.flight_land_baseline_24,
        headline = stringResource(R.string.flight_arrival_tile),
        supporting = airportName,
    )
}

@Composable
@Preview
fun ArrivalEventListItemPreview() {
    AppTheme {
        ArrivalEventListItem(
            showDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            airportName = "John F Kennedy",
        )
    }
}