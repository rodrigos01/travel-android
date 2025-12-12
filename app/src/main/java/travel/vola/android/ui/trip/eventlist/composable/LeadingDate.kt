package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@Composable
fun LeadingDate(
    dayOfMonth: String,
    dayOfWeek: String,
    modifier: Modifier = Modifier,
    showSmall: Boolean = false,
    highlightDate: Boolean = false,
) {
    val dateBackground by animateColorAsState(targetValue = if (highlightDate) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier.then(
                if (!showSmall) {
                    Modifier
                        .background(
                            color = dateBackground,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                        .size(40.dp)
                } else {
                    Modifier
                }
            )
        ) {
            Text(
                text = dayOfMonth,
                style = if (showSmall) MaterialTheme.typography.labelLarge else MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = dayOfWeek,
            style = if (showSmall) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
@PreviewLightDark
fun LeadingDatePreview() {
    AppTheme {
        LeadingDate(dayOfMonth = "21", dayOfWeek = "Tue", highlightDate = true)
    }
}
