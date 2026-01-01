package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.SuggestionPlaceholderItemState
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SuggestionPlaceholderListItem(
    state: SuggestionPlaceholderItemState,
    highlightDate: Boolean = false
) {
    EventItem(
        showDate = state.showDate,
        highlightDate = highlightDate,
        dayOfMonthString = state.dayOfMonth,
        dayOfWeekString = state.dayOfWeek,
        position = state.backgroundStyle.asEventItemPosition(),
        containerColor = Color.Transparent,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Generating...", modifier = Modifier.weight(1F))
            LoadingIndicator()
        }
    }
}

@Composable
@Preview
fun SuggestionPlaceholderListItemPreview() {
    AppTheme {
        SuggestionPlaceholderListItem(
            SuggestionPlaceholderItemState(
                timestamp = ZonedDateTime.now(),
                backgroundStyle = TripItemState.EventItemState.BackgroundStyle.SINGLE,
                showDate = true,
                dayOfMonth = "17",
                dayOfWeek = "Fri",
                sectionId = ""
            )
        )
    }
}