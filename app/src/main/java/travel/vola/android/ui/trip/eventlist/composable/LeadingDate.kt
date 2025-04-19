package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme

@Composable
fun LeadingDate(
    dayOfMonth: String,
    dayOfWeek: String,
    modifier: Modifier = Modifier,
    showSmall: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = dayOfMonth,
            style = if (showSmall) MaterialTheme.typography.labelLarge else MaterialTheme.typography.headlineSmall,
        )
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
        LeadingDate(dayOfMonth = "21", dayOfWeek = "Tue")
    }
}
