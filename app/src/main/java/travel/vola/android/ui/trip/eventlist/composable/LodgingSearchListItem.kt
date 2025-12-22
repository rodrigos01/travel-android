package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchParams
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.state.SearchResultItemState
import java.time.ZonedDateTime

@Composable
fun LodgingSearchListItem(
    checkIn: ZonedDateTime,
    checkOut: ZonedDateTime? = null,
    minCheckIn: ZonedDateTime? = null,
    minCheckOut: ZonedDateTime? = null,
    locationText: String? = null,
    searchResults: List<SearchResultItemState> = emptyList(),
    onSwitchToManualButtonTapped: () -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onUpdated: (
        checkIn: ZonedDateTime,
        checkOut: ZonedDateTime?,
        selectedSearchResultIndex: Int,
    ) -> Unit,
) {
    var checkInState by remember {
        mutableStateOf(checkIn)
    }
    var checkOutState by remember {
        mutableStateOf(checkOut)
    }
    var selectedSearchResultIndexState by remember {
        mutableIntStateOf(-1)
    }
    LaunchedEffect(
        checkInState, checkOutState, selectedSearchResultIndexState
    ) {
        onUpdated(
            checkInState,
            checkOutState,
            selectedSearchResultIndexState,
        )
    }
    Column {
        LodgingSearchParams(checkIn,
            minCheckIn,
            checkOut,
            minCheckOut,
            locationText,
            searchResults,
            onCheckInDateSelected = { checkInState = it },
            onCheckOutDateSelected = { checkOutState = it },
            onLocationSearchTextChanged,
            onLocationSearchResultSelected = { selectedSearchResultIndexState = it })
        TextButton(
            onClick = onSwitchToManualButtonTapped,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("or enter lodging details manually")
        }
    }
}

@Composable
@Preview
fun LodgingSearchListItemPreview() {
    AppTheme {
        AddPlanScaffold(AddPlanType.Lodging, {}, true, false, {}, true, "Save", {}, "Cancel", {}) {
            LodgingSearchListItem(checkIn = zonedDateTime("2025-12-05T12:00 +0100"),
                checkOut = null,
                searchResults = List(5) { SearchResultItemState("City$it", "Address$it") },
                locationText = null,
                onLocationSearchTextChanged = {},
                onSwitchToManualButtonTapped = {},
                onUpdated = { _, _, _ -> })
        }
    }
}
