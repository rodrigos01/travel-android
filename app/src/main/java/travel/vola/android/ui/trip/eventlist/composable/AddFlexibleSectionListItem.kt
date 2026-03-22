package travel.vola.android.ui.trip.eventlist.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import java.time.ZonedDateTime

@Composable
fun AddFlexibleSectionListItem(
    state: AddFlexibleSectionItemState,
    onTextChanged: (CharSequence) -> Unit,
    onGenerateTapped: () -> Unit,
    onUpdated: (
        dateTime: ZonedDateTime,
        timeSelected: Boolean,
    ) -> Unit,
) {
    Column {
        AddPlanRow(
            initialDateTime = state.startDateTime,
            timeSelectedInitially = state.hasStartTime,
            searchResults = emptyList(),
            title = {},
            timeSelectorLabel = stringResource(R.string.action_pick_time),
            labelText = "Section Name",
            text = state.sectionName,
            placeHolder = "Section Name",
            onTextChanged = onTextChanged,
            onUpdated = { dateTime, timeSelected, _ ->
                if (dateTime != null) {
                    onUpdated(dateTime, timeSelected)
                }
            }
        )
        IconTextButton(
            onClick = onGenerateTapped,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Generate suggestions")
            Text(stringResource(R.string.action_generate_suggestions))
        }
    }
}

@PreviewLightDark
@Composable
fun AddFlexibleSectionListItemPreview() {
    AppTheme {
        Surface {
            AddFlexibleSectionListItem(
                state = AddFlexibleSectionItemState(
                    id = "id",
                    typeSelectionEnabled = true,
                    dateSelectionEnabled = true,
                    saveButtonEnabled = true,
                    deleteButtonEnabled = true,
                    startDateTime = ZonedDateTime.now(),
                    hasStartTime = true,
                    sectionName = "My New Section"
                ),
                onTextChanged = {},
                onGenerateTapped = {},
                onUpdated = { _, _ -> },
            )
        }
    }
}