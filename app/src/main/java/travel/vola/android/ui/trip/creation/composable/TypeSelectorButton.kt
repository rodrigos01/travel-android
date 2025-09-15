package travel.vola.android.ui.trip.creation.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import travel.vola.android.R
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.ui.theme.AppTheme

enum class AddPlanType(
    internal val icon: @Composable () -> ImageVector,
    internal val label: String,
) {
    Place(Icons.Default.LocationOn, "Place"),
    Flight(R.drawable.flight_baseline_24, "Flight"),
    Lodging(R.drawable.hotel_baseline_24, "Lodging"),
    Restaurant(R.drawable.restaurant_baseline_24, "Restaurant");

    constructor(@DrawableRes drawableId: Int, label: String) : this(
        icon = {
            ImageVector.vectorResource(
                drawableId
            )
        },
        label = label,
    )

    constructor(icon: ImageVector, label: String) : this({ icon }, label)
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
        IconTextButton(
            onClick = { showTypeSelectorMenu = true },
            enabled = enabled,
            leadingIcon = type.icon(),
            trailingIcon = Icons.Default.ArrowDropDown.takeIf { enabled }
        ) {
            Text(type.label, maxLines = 1)
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
                        Icon(
                            option.icon(),
                            contentDescription = null,
                            tint = LocalContentColor.current,
                        )
                    },
                    onClick = {
                        selection = option
                        onOptionSelected(option)
                        showTypeSelectorMenu = false
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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            TypeSelectorButton(AddPlanType.Lodging, {})
        }
    }
}
