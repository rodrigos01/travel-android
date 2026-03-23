package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyDateListItem(
    dayOfMonth: String,
    dayOfWeek: String,
    highlightDate: Boolean,
    isGeneratingSuggestions: Boolean,
    onTap: () -> Unit,
    onGenerateTapped: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = if (!isGeneratingSuggestions) {
            Modifier.clickable(onClick = onTap)
        } else {
            Modifier
        }
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        LeadingDate(
            dayOfMonth = dayOfMonth,
            dayOfWeek = dayOfWeek,
            highlightDate = highlightDate,
        )
        AnimatedContent(isGeneratingSuggestions) { generating ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (generating) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.state_generating))
                    LoadingIndicator()
                } else {
                    Text(
                        text = "No plans yet, tap to add",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )
                    FilledTonalIconButton(onClick = onGenerateTapped) {
                        Icon(
                            Icons.Rounded.AutoFixHigh,
                            contentDescription = "generate plans",
                            tint = LocalContentColor.current,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun EmptyDateListItemPreview() {
    AppTheme {
        EmptyDateListItem(
            "12",
            "Sat",
            highlightDate = true,
            isGeneratingSuggestions = false,
            onTap = {},
            onGenerateTapped = {},
        )
    }
}
