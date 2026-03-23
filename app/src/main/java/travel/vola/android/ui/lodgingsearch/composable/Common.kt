package travel.vola.android.ui.lodgingsearch.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LodgingImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    colorFilter: ColorFilter? = null,
) {
    Image(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        painter = painter,
        contentDescription = "Lodging Image Description",
        contentScale = contentScale,
        colorFilter = colorFilter,
    )
}

@Composable
fun ButtonContent(
    @DrawableRes iconResId: Int? = null,
    icon: ImageVector? = null,
    iconContentDescription: String,
    text: String,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = iconContentDescription)
        } else if (iconResId != null) {
            Icon(
                painterResource(iconResId),
                contentDescription = iconContentDescription,
            )
        }
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
