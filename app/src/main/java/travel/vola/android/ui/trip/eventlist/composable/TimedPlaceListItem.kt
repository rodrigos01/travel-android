package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.extensions.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState

@Composable
fun TimedPlaceListItem(state: TripItemState.TimedPlaceItemState) {
    EventListItem(
        showDate = state.showDate,
        dayOfMonthString = state.dayOfMonth,
        dayOfWeekString = state.dayOfWeek,
        timeString = state.time,
        iconPainter = rememberVectorPainter(Icons.Default.Place),
        headline = state.placeName,
        supporting = state.cityName,
    )
}

@Composable
@PreviewLightDark
fun TimedPlaceListItemPreview() {
    AppTheme {
        TimedPlaceListItem(
            TripItemState.TimedPlaceItemState(
                id = "",
                timestamp = Time("2025-10-17T15:30:00Z"),
                showDate = true,
                dayOfMonth = "17",
                dayOfWeek = "Fri",
                time = "15:30 AM",
                placeName = "Fushimi Inari Taisha",
                cityName = "Kyoto, Japan",
                imageUrl = ""
            )
        )
    }
}
