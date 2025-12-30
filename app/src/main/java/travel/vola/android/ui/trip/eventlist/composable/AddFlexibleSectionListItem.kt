package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import java.time.ZonedDateTime

@Composable
fun AddFlexibleSectionListItem(
    state: AddFlexibleSectionItemState,
    onTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        dateTime: ZonedDateTime,
        timeSelected: Boolean,
    ) -> Unit,
) {
    AddPlanRow(
        initialDateTime = state.startDateTime,
        timeSelectedInitially = state.hasStartTime,
        searchResults = emptyList(),
        title = {},
        timeSelectorLabel = "Pick Time",
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
                onUpdated = { _, _ -> },
            )
        }
    }
}