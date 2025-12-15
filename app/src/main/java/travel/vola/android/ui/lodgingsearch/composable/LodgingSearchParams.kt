package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import travel.vola.android.R
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.common.ui.components.SearchBox
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.extensions.dateString
import travel.vola.android.model.data.Time
import travel.vola.android.ui.trip.creation.composable.DatePickerDialog
import travel.vola.android.ui.trip.state.SearchResultItemState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LodgingSearchParams(
    checkIn: Time? = null,
    minCheckIn: Time? = null,
    checkOut: Time? = null,
    minCheckOut: Time? = null,
    locationText: String? = null,
    searchResults: List<SearchResultItemState> = emptyList(),
    onCheckInDateSelected: (Time) -> Unit,
    onCheckOutDateSelected: (Time) -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            DatePickerTextButton(
                checkIn, minCheckIn, label = "check-in date", onCheckInDateSelected
            )
            DatePickerTextButton(
                checkOut, minCheckOut, label = "check-out date", onCheckOutDateSelected
            )
        }
        var showSearchDialog by remember { mutableStateOf(false) }
        var buttonLabel by remember { mutableStateOf(locationText) }
        FilledTonalButton(
            onClick = {
                showSearchDialog = true
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(buttonLabel ?: "Tap to enter location")
        }
        AnimatedVisibility(visible = showSearchDialog) {
            Dialog(onDismissRequest = {
                showSearchDialog = false
            }) {
                var query by remember { mutableStateOf(locationText ?: "") }
                val focusRequester = remember { FocusRequester() }
                SearchBox(
                    query = query,
                    placeHolder = { Text("Enter location") },
                    searchResults = searchResults.map { SearchResult(it.title, it.subtitle) },
                    onQueryChange = {
                        query = it
                        onLocationSearchTextChanged(it)
                    },
                    onResultTapped = {
                        buttonLabel = "${searchResults[it].title}, ${searchResults[it].subtitle}"
                        showSearchDialog = false
                        onLocationSearchResultSelected(it)
                    },
                    focusRequester = focusRequester,
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
private fun DatePickerTextButton(
    time: Time?,
    minTime: Time?,
    label: String,
    onTimeSelected: (Time) -> Unit,
) {
    var selectedTime by remember { mutableStateOf(time) }
    var showDatePickerState by remember { mutableStateOf(false) }
    IconTextButton(
        onClick = {
            showDatePickerState = true
        },
        leadingIcon = ImageVector.vectorResource(R.drawable.today_baseline_24),
    ) {
        Text(selectedTime?.dateString() ?: label)
    }
    if (showDatePickerState) {
        DatePickerDialog(
            selectedTime = selectedTime,
            minimumSelectableTime = minTime,
            onDateSelected = {
                selectedTime = it
                onTimeSelected(it)
            },
            onDismiss = {
                showDatePickerState = false
            })
    }
}

@Preview
@Composable
fun LodgingSearchParamsPreview() {
    var searchResults by remember { mutableStateOf(emptyList<SearchResultItemState>()) }
    Surface(modifier = Modifier.fillMaxSize()) {
        LodgingSearchParams(
            searchResults = searchResults,
            onCheckInDateSelected = {},
            onCheckOutDateSelected = {},
            onLocationSearchTextChanged = { query ->
                searchResults = List(query.length * 5) {
                    SearchResultItemState("City$it", "country$it")
                }
            },
            onLocationSearchResultSelected = {},
        )
    }
}