package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AutoCompleteResultState

@Composable
fun AddPlaceListItem(
    state: AddPlanRowState,
    placeName: String?,
    searchResults: List<AutoCompleteResultState>,
    onTextChanged: (CharSequence) -> Unit,
) {
    AddPlanRow(
        state = state,
        title = { Text("Arrival") },
        labelText = "Location",
        placeHolder = "Enter Location",
        text = placeName,
        onTextChanged = { onTextChanged(it.toString()) },
        searchResults = searchResults,
        showTimePickerButton = false,
    )
}

@PreviewLightDark
@Composable
fun AddPlaceListItemPreview() {
    AppTheme {
        Surface {
            AddPlaceListItem(
                state = rememberAddPlanRowState(),
                placeName = "New York",
                searchResults = emptyList(),
                onTextChanged = {},
            )
        }
    }
}