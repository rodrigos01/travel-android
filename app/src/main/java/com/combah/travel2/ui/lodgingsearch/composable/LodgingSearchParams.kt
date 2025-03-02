package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.combah.travel2.R
import com.combah.travel2.extensions.dateString
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.common.components.IconTextButton
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton
import com.combah.travel2.ui.trip.state.SearchResultItemState

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
    onLocationSearchResultSelected: (Int) -> Unit
) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            DatePickerTextButton(
                checkIn,
                minCheckIn,
                label = "check-in date",
                onCheckInDateSelected
            )
            DatePickerTextButton(
                checkOut,
                minCheckOut,
                label = "check-out date",
                onCheckOutDateSelected
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
                val expanded =
                    searchResults.isNotEmpty() && query.isNotBlank() && query.isNotEmpty()
                val focusRequester = remember { FocusRequester() }
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query,
                            placeholder = { Text("Enter location") },
                            onQueryChange = {
                                query = it
                                onLocationSearchTextChanged(it)
                            },
                            expanded = expanded,
                            onExpandedChange = {},
                            onSearch = {},
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    },
                    expanded = expanded,
                    onExpandedChange = {},
                    colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    searchResults.forEachIndexed { index, result ->
                        Column(modifier = Modifier
                            .clickable {
                                buttonLabel = "${result.title}, ${result.subtitle}"
                                showSearchDialog = false
                                onLocationSearchResultSelected(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()) {
                            Text(result.title, style = MaterialTheme.typography.labelMedium)
                            Text(result.subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                LaunchedEffect(showSearchDialog) {
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
    DatePickerButton(
        selectedTime = selectedTime,
        minimumSelectableTime = minTime,
        onDateSelected = {
            selectedTime = it
            onTimeSelected(it)
        },
    ) {
        IconTextButton(
            onClick = {},
            leadingIconResId = R.drawable.baseline_today_24,
        ) {
            Text(selectedTime?.dateString() ?: label)
        }
    }
}