package travel.vola.android.common.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vola.android.ui.theme.AppTheme

@Composable
fun IconTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes leadingIconResId: Int? = null,
    @DrawableRes trailingIconResId: Int? = null,
    content: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        enabled = enabled,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIconResId?.let {
                Image(
                    painter = painterResource(id = it),
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            content()
            trailingIconResId?.let {
                Image(
                    painter = painterResource(id = it),
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically)
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