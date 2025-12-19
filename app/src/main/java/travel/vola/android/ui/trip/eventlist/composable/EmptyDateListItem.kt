package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@Composable
fun EmptyDateListItem(
    dayOfMonth: String,
    dayOfWeek: String,
    highlightDate: Boolean,
    onTap: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(onClick = onTap)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        LeadingDate(
            dayOfMonth = dayOfMonth,
            dayOfWeek = dayOfWeek,
            highlightDate = highlightDate,
        )
        Text(
            text = "No plans yet, tap to add",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
@Preview
fun EmptyDateListItemPreview() {
    AppTheme {
        EmptyDateListItem("12", "Sat", true, onTap = {})
    }
}
