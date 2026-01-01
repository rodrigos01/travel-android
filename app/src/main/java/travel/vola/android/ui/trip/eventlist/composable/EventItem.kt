package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState

enum class EventItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE,
}

enum class EventItemStyle {
    Filled, Outlined,
}

@Composable
fun EventItem(
    showDate: Boolean = false,
    highlightDate: Boolean = false,
    dayOfMonthString: String?,
    dayOfWeekString: String?,
    position: EventItemPosition = EventItemPosition.MIDDLE,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    style: EventItemStyle = EventItemStyle.Filled,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        if (showDate && dayOfMonthString != null && dayOfWeekString != null) {
            LeadingDate(
                dayOfMonth = dayOfMonthString,
                dayOfWeek = dayOfWeekString,
                showSmall = false,
                highlightDate = highlightDate,
            )
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
        val roundedCornerRadius = 12.dp
        val separatorPadding = 2.dp
        val shape = RoundedCornerShape(
            topStart = if (position == EventItemPosition.TOP || position == EventItemPosition.SINGLE) roundedCornerRadius else 0.dp,
            topEnd = if (position == EventItemPosition.TOP || position == EventItemPosition.SINGLE) roundedCornerRadius else 0.dp,
            bottomStart = if (position == EventItemPosition.BOTTOM || position == EventItemPosition.SINGLE) roundedCornerRadius else 0.dp,
            bottomEnd = if (position == EventItemPosition.BOTTOM || position == EventItemPosition.SINGLE) roundedCornerRadius else 0.dp,
        )
        Box(
            modifier = Modifier
                .padding(
                    start = 8.dp,
                    top = if (position == EventItemPosition.TOP || position == EventItemPosition.SINGLE) 4.dp else separatorPadding,
                    bottom = if (position == EventItemPosition.BOTTOM || position == EventItemPosition.SINGLE) 4.dp else separatorPadding,
                )
                .then(
                    when (style) {
                        EventItemStyle.Filled -> Modifier
                            .clip(shape)
                        EventItemStyle.Outlined -> Modifier.border(
                            ButtonDefaults.outlinedButtonBorder(
                                enabled = true
                            ),
                            shape = shape,
                        )
                    }
                )
                .background(color = containerColor, shape = shape),
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

fun TripItemState.EventItemState.BackgroundStyle.asEventItemPosition() = when (this) {
    TripItemState.EventItemState.BackgroundStyle.TOP -> EventItemPosition.TOP
    TripItemState.EventItemState.BackgroundStyle.MIDDLE -> EventItemPosition.MIDDLE
    TripItemState.EventItemState.BackgroundStyle.BOTTOM -> EventItemPosition.BOTTOM
    TripItemState.EventItemState.BackgroundStyle.SINGLE -> EventItemPosition.SINGLE
}

@Composable
@PreviewLightDark
fun EventItemPreview() {
    AppTheme {
        EventItem(
            showDate = true,
            highlightDate = true,
            dayOfMonthString = "21",
            dayOfWeekString = "Wed",
            position = EventItemPosition.SINGLE,
            style = EventItemStyle.Outlined,
        ) {
            Text("An Event")
        }
    }
}
