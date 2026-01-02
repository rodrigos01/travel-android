package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import travel.vola.android.common.ui.components.InlinedTextField
import travel.vola.android.common.ui.components.rememberPlaceholderPainter
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.TripItemState


@Composable
fun PlaceDetailsListItem(
    state: TripItemState.PlaceDetailsItemState,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    onNoteAdded: (String) -> Unit = {},
) {
    PlaceDetailsListItem(
        title = state.name,
        subtitle = state.subtitle,
        imageUrl = state.imageUrl,
        note = state.note,
        actions = actions,
        modifier = modifier,
        onNoteAdded = onNoteAdded,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaceDetailsListItem(
    title: String,
    subtitle: String,
    imageUrl: String,
    note: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    onNoteAdded: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            placeholder = rememberPlaceholderPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .aspectRatio(16 / 9F),
        )
        Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)) {
            Row {
                Column(Modifier.weight(1F)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                actions()
            }
        }
        val hasNote = note.isNotBlank()
        InlinedTextField(
            onDone = onNoteAdded,
            initialValue = note,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (hasNote) {
                    LocalContentColor.current
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            ),
        ) {
            Text(
                text = if (hasNote) note else "Add note", style = if (hasNote) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    ButtonDefaults.textStyleFor(ButtonDefaults.MinHeight)
                }
            )
        }
    }
}

@Preview
@Composable
fun PlaceDetailsListItemPreview() {
    AppTheme {
        PlaceDetailsListItem(
            TripItemState.PlaceDetailsItemState(
                "Place Name",
                "1234, place address lane, place city, PS, 101234",
                "",
                "",
            ),
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "close",
                    )
                }
            },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        )
    }
}