package travel.vola.android.common.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.PopupProperties

data class SelectorButtonOption(
    val icon: ImageVector,
    val label: String,
)

@Composable
fun SelectorButton(
    options: List<SelectorButtonOption>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    onOptionSelected: (SelectorButtonOption) -> Unit,
) {
    Box(
        modifier = modifier,
    ) {
        var showTypeSelectorMenu by remember { mutableStateOf(false) }
        IconTextButton(
            onClick = { showTypeSelectorMenu = true },
            enabled = enabled,
            leadingIcon = leadingIcon,
            trailingIcon = Icons.Default.ArrowDropDown.takeIf { enabled }
        ) {
            Text(options[selectedIndex].label, maxLines = 1)
        }
        DropdownMenu(
            expanded = showTypeSelectorMenu,
            onDismissRequest = { showTypeSelectorMenu = false },
            properties = PopupProperties(focusable = false)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    leadingIcon = {
                        Icon(
                            option.icon,
                            contentDescription = null,
                            tint = LocalContentColor.current,
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        showTypeSelectorMenu = false
                    },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSecondaryContainer),
                )
            }
        }
    }
}