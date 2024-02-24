package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun DateRangeListItem(
    dayOfMonthStart: String,
    dayOfWeekStart: String,
    dayOfMonthEnd: String,
    dayOfWeekEnd: String,
    onAddButtonClick: () -> Unit,
) {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        ListItem(
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LeadingDate(
                        dayOfMonth = dayOfMonthStart,
                        dayOfWeek = dayOfWeekStart,
                        showSmall = true,
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        thickness = 1.dp,
                        modifier = Modifier
                            .width(24.dp)
                            .padding(horizontal = 8.dp),
                    )
                    LeadingDate(
                        dayOfMonth = dayOfMonthEnd,
                        dayOfWeek = dayOfWeekEnd,
                        showSmall = true,
                    )
                    Spacer(modifier = Modifier.weight(1F))
                    TextButton(
                        onClick = { onAddButtonClick() },
                    ) {
                        Text(text = "Add Plans")
                    }
                }
            })
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
@Preview
fun DateRangeListItemPreview() {
    AppTheme {
        DateRangeListItem("12", "Sat", "20", "Mon", {})
    }
}