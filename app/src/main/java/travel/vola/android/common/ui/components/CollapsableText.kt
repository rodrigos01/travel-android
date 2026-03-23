package travel.vola.android.common.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CollapsableText(text: String, collapsed: Boolean, collapsedMaxLines: Int = 6) {
}

@Composable
fun CollapsableText(text: String, collapsedMaxLines: Int = 6) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        var expanded by remember { mutableStateOf(true) }
        var hasMoreText by remember { mutableStateOf(false) }
        Text(
            text,
            maxLines = if (!expanded) collapsedMaxLines else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                if (!hasMoreText && it.lineCount > collapsedMaxLines) {
                    hasMoreText = true
                    expanded = false
                }
            },
            modifier = Modifier.animateContentSize(),
        )
        if (hasMoreText) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(if (expanded) "Read less" else "Read more")
            }
        }
    }
}
