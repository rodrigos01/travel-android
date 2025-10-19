package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
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
    onTap: () -> Unit,
) {
    ListItem(headlineContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                LeadingDate(
                    dayOfMonth = dayOfMonth,
                    dayOfWeek = dayOfWeek,
                )
            }
            Text(
                text = "No plans yet, tap to add",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }, modifier = Modifier.clickable(onClick = onTap))
}

@Composable
@Preview
fun EmptyDateListItemPreview() {
    AppTheme {
        EmptyDateListItem("12", "Sat", onTap = {})
    }
}