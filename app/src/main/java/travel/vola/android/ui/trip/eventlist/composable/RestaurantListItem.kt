package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.common.ui.components.Restaurant
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState

@Composable
fun RestaurantListItem(state: TripItemState.RestaurantReservationItemState, highlightDate: Boolean = false) {
    EventListItem(
        showDate = state.showDate,
        highlightDate = highlightDate,
        dayOfMonthString = state.dayOfMonth,
        dayOfWeekString = state.dayOfWeek,
        timeString = state.time,
        iconPainter = rememberVectorPainter(Icons.Default.Restaurant),
        headline = state.restaurantName,
        supporting = state.restaurantAddress,
        position = state.backgroundStyle.asEventItemPosition(),
    )
}

@Composable
@PreviewLightDark
fun RestaurantListItemPreview() {
    AppTheme {
        RestaurantListItem(
            TripItemState.RestaurantReservationItemState(
                id = "",
                timestamp = zonedDateTime("2025-10-17T15:30:00Z"),
                showDate = true,
                dayOfMonth = "17",
                dayOfWeek = "Fri",
                time = "15:30 AM",
                restaurantName = "Le relais de venise - L'Entrecot",
                restaurantAddress = "271 Bd Pereire",
            )
        )
    }
}
