package com.combah.travel2.ui.trip.creation.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.combah.travel2.R
import com.combah.travel2.ui.theme.AppTheme

enum class AddPlanType(
    @DrawableRes internal val drawableId: Int,
    internal val label: String,
) {
    Flight(R.drawable.ic_flight_24dp, "Flight"),
    Lodging(R.drawable.ic_hotel_black_24dp, "Lodging")
}

@Composable
fun TypeSelectorButton(
    initialType: AddPlanType,
    onOptionSelected: (AddPlanType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var selection: AddPlanType? by remember {
        mutableStateOf(null)
    }
    val type = remember(initialType, selection) {
        selection ?: initialType
    }
    Box(
        modifier = modifier,
    ) {
        var showTypeSelectorMenu by remember { mutableStateOf(false) }
        TextButton(
            onClick = { showTypeSelectorMenu = true },
            shape = RoundedCornerShape(8.dp),
            enabled = enabled,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = type.drawableId),
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(initialType.label, maxLines = 1)
                if (enabled) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        DropdownMenu(
            expanded = showTypeSelectorMenu,
            onDismissRequest = { showTypeSelectorMenu = false },
            properties = PopupProperties(focusable = false)
        ) {
            AddPlanType.entries.toTypedArray().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = option.drawableId),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                        )
                    },
                    onClick = {
                        selection = option
                        onOptionSelected(option)
                    },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSecondaryContainer),
                )
            }
        }
    }
}

@Composable
@Preview
fun TypeSelectorButtonPreview() {
    AppTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)
        ) {
            TypeSelectorButton(AddPlanType.Lodging, {})
        }
    }
}
