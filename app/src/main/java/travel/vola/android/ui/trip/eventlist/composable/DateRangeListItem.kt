package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DateRangeListItem(
    dayOfMonthStart: String,
    dayOfWeekStart: String,
    dayOfMonthEnd: String,
    dayOfWeekEnd: String,
    focused: Boolean,
    isGeneratingSuggestions: Boolean,
    onAddButtonClick: () -> Unit,
    onGenerateButtonClick: () -> Unit,
) {
    ListItem(headlineContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeadingDate(
                dayOfMonth = dayOfMonthStart,
                dayOfWeek = dayOfWeekStart,
                showSmall = true,
                highlightDate = focused
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
            AnimatedContent(isGeneratingSuggestions) { generating ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (generating) {
                        Text("Generating")
                        LoadingIndicator()
                    } else {
                        TextButton(onClick = onAddButtonClick) {
                            Text(text = "Add Plans")
                        }
                        FilledTonalIconButton(onClick = onGenerateButtonClick) {
                            Icon(
                                Icons.Rounded.AutoFixHigh,
                                contentDescription = "generate plans",
                                tint = LocalContentColor.current
                            )
                        }
                    }
                }
            }
        }
    })
}

@Composable
@Preview
fun DateRangeListItemPreview() {
    AppTheme {
        DateRangeListItem(
            "12",
            "Sat",
            "20",
            "Mon",
            focused = true,
            isGeneratingSuggestions = true,
            onAddButtonClick = {},
            onGenerateButtonClick = {})
    }
}
