package travel.vola.android.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@Composable
fun IconTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconSize: Dp = 24.dp,
    trailingIcon: ImageVector? = null,
    trailingIconSize: Dp = 24.dp,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    shape: Shape = ButtonDefaults.textShape,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = shape,
        colors = colors,
        enabled = enabled,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(
                    it,
                    tint = LocalContentColor.current,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(leadingIconSize)
                )
            }
            content()
            trailingIcon?.let {
                Icon(
                    it,
                    tint = LocalContentColor.current,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(trailingIconSize)
                )
            }
        }
    }
}

@Composable
@Preview
fun IconTextButtonPreview() {
    AppTheme {
        IconTextButton(onClick = {}) {
            Text("My Button")
        }
    }
}