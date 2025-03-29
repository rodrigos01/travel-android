package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@Composable
fun CheckoutListItem(
    showDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    timeString: String,
    hotelName: String,
) {
    EventListItem(
        showDate,
        dayOfMonthString,
        dayOfWeekString,
        timeString,
        icon = R.drawable.ic_hotel_black_24dp,
        headline = stringResource(id = R.string.hotel_checkout_title),
        supporting = hotelName,
    )
}

@Composable
@Preview
fun CheckoutListItemPreview() {
    AppTheme {
        CheckoutListItem(
            showDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Fri",
            timeString = "6:15 AM",
            hotelName = "Hotel des Arms",
        )
    }
}