package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
    showBottomDivider: Boolean,
    onTap: () -> Unit,
) {
    Column(modifier = Modifier.clickable(onClick = onTap)) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
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
        })
        if (showBottomDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
@Preview
fun EmptyDateListItemPreview() {
    AppTheme {
        EmptyDateListItem("12", "Sat", showBottomDivider = true, {})
    }
}