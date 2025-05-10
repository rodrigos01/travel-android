package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.time.ZonedDateTime

class AddPlaceListItemState internal constructor(
    val startState: AddPlanRowState,
    val endState: AddPlanRowState,
    private val hasEndState: MutableState<Boolean>,
) {
    var hasEnd: Boolean
        get() = hasEndState.value
        internal set(value) {
            hasEndState.value = value
        }
}

@Composable
fun rememberAddPlaceListItemState(
    startState: AddPlanRowState = rememberAddPlanRowState(),
    endState: AddPlanRowState = rememberAddPlanRowState(),
    hasEnd: Boolean = false,
) = remember(startState, endState, hasEnd) {
    AddPlaceListItemState(
        startState,
        endState,
        mutableStateOf(hasEnd),
    )
}

@Composable
fun AddPlaceListItem(
    state: AddPlaceListItemState,
    placeName: String?,
    searchResults: List<AutoCompleteResultState>,
    onTextChanged: (CharSequence) -> Unit,
    minEndTime: ZonedDateTime? = null,
) {
    Column {
        AddPlanRow(
            state = state.startState,
            title = if (state.hasEnd) {
                { Text("Start") }
            } else {
                {}
            },
            labelText = "Location",
            placeHolder = "Enter Location",
            text = placeName,
            onTextChanged = { onTextChanged(it.toString()) },
            searchResults = searchResults,
            timeSelectorLabel = "Pick Time",
            showTextField = true,
        )
        if (state.hasEnd) {
            AddPlanRow(
                state = state.endState,
                title = { Text("End") },
                timeSelectorLabel = "Pick Time",
                minTime = minEndTime,
            )
        }
        TextButton(onClick = { state.hasEnd = !state.hasEnd }) {
            Text(if (!state.hasEnd) "Set end time" else "Remove end time")
        }
    }
}

@PreviewLightDark
@Composable
fun AddPlaceListItemPreview() {
    AppTheme {
        Surface {
            AddPlaceListItem(
                state = rememberAddPlaceListItemState(),
                placeName = "New York",
                searchResults = emptyList(),
                onTextChanged = {},
            )
        }
    }
}