package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@Composable
fun EmptyAddPlanListItem(showDivider: Boolean = true, onAddButtonClick: () -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        TextButton(
            onClick = { onAddButtonClick() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Add Plans")
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
@Preview
fun EmptyAddPlanListItemPreview() {
    AppTheme {
        EmptyAddPlanListItem(showDivider = true, onAddButtonClick = {})
    }
}
