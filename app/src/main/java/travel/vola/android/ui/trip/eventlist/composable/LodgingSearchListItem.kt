package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vola.android.extensions.Time
import com.vola.android.model.data.Time
import com.vola.android.ui.lodgingsearch.composable.LodgingSearchParams
import com.vola.android.ui.theme.AppTheme
import com.vola.android.ui.trip.creation.composable.AddPlanType
import com.vola.android.ui.trip.state.SearchResultItemState

@Composable
fun LodgingSearchListItem(
    checkIn: Time? = null,
    checkOut: Time? = null,
    minCheckIn: Time? = null,
    minCheckOut: Time? = null,
    locationText: String? = null,
    searchResults: List<SearchResultItemState> = emptyList(),
    onCheckInDateSelected: (Time) -> Unit,
    onCheckOutDateSelected: (Time) -> Unit,
    onSwitchToManualButtonTapped: () -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
) {
    Column {
        LodgingSearchParams(
            checkIn,
            minCheckIn,
            checkOut,
            minCheckOut,
            locationText,
            searchResults,
            onCheckInDateSelected,
            onCheckOutDateSelected,
            onLocationSearchTextChanged,
            onLocationSearchResultSelected
        )
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
            LodgingSearchListItem(
                checkIn = Time("2025-12-05T12:00 +0100"),
                checkOut = null,
                searchResults = List(5) { SearchResultItemState("City$it", "Address$it") },
                locationText = null,
                onCheckInDateSelected = {},
                onCheckOutDateSelected = {},
                onLocationSearchTextChanged = {},
                onLocationSearchResultSelected = {},
                onSwitchToManualButtonTapped = {},
            )
        }
    }
}
