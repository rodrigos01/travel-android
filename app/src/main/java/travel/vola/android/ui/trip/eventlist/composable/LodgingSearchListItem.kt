package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchParams
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.SearchResultItemState

@Composable
fun LodgingSearchListItem(
    uiState: LodgingSearchItemState,
    onSwitchToManualButtonTapped: () -> Unit,
    onUpdated: (LodgingSearchItemState) -> Unit,
) {
    Column {
        LodgingSearchParams(
            checkIn = uiState.checkIn,
            minCheckIn = null,
            checkOut = uiState.checkOut,
            minCheckOut = uiState.minCheckOutTime,
            locationText = uiState.locationText,
            searchResults = uiState.searchResults,
            onCheckInDateSelected = { onUpdated(uiState.copy(checkIn = it)) },
            onCheckOutDateSelected = { onUpdated(uiState.copy(checkOut = it)) },
            onLocationSearchTextChanged = {
                onUpdated(uiState.copy(locationText = it.toString(), selectedResultId = null))
            },
            onLocationSearchResultSelected = { index ->
                onUpdated(uiState.copy(selectedResultId = uiState.searchResults.getOrNull(index)?.id))
            },
        )
        TextButton(
            onClick = onSwitchToManualButtonTapped,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(stringResource(R.string.prompt_enter_lodging_manually))
        }
    }
}

@Composable
@Preview
fun LodgingSearchListItemPreview() {
    AppTheme {
        AddPlanScaffold(AddPlanType.Lodging, {}, true, false, {}, true, "Save", {}, "Cancel", {}) {
            LodgingSearchListItem(
                uiState = LodgingSearchItemState(
                    id = "",
                    timestamp = zonedDateTime("2025-12-05T12:00 +0100"),
                    typeSelectionEnabled = true,
                    saveButtonEnabled = false,
                    deleteButtonEnabled = false,
                    dateSelectionEnabled = true,
                    locationText = null,
                    searchResults = List(5) { SearchResultItemState("id$it", "City$it", "Address$it") },
                    checkIn = zonedDateTime("2025-12-05T12:00 +0100"),
                    minCheckOutTime = null,
                    checkOut = null,
                ),
                onSwitchToManualButtonTapped = {},
                onUpdated = {},
            )
        }
    }
}
