package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.combah.travel2.R
import com.combah.travel2.extensions.dateString
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.common.components.IconTextButton
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingSearchListItem(
    checkIn: Time? = null,
    checkOut: Time? = null,
    minCheckIn: Time? = null,
    minCheckOut: Time? = null,
    locationText: String? = null,
    searchResults: List<String> = emptyList(),
    onCheckInDateSelected: (Time) -> Unit,
    onCheckOutDateSelected: (Time) -> Unit,
    onSwitchToManualButtonTapped: () -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
) {
    Column {
        Row {
            DatePickerButton(
                minimumSelectableTime = minCheckIn,
                onDateSelected = onCheckInDateSelected,
                modifier = Modifier.weight(1F),
            ) {
                IconTextButton(
                    onClick = {},
                    leadingIconResId = R.drawable.baseline_today_24,
                ) {
                    Text(checkIn?.dateString() ?: "check-in date")
                }
            }
            DatePickerButton(
                minimumSelectableTime = minCheckOut,
                onDateSelected = onCheckOutDateSelected,
                modifier = Modifier.weight(1F),
            ) {
                IconTextButton(
                    onClick = {},
                    leadingIconResId = R.drawable.baseline_today_24,
                ) {
                    Text(checkOut?.dateString() ?: "check-out date")
                }
            }
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
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = "",
                            placeholder = { Text("Enter location") },
                            onQueryChange = onLocationSearchTextChanged,
                            expanded = true,
                            onExpandedChange = {},
                            onSearch = {}
                        )
                    },
                    expanded = true,
                    onExpandedChange = {},
                    colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    searchResults.forEachIndexed { index, result ->
                        Text(result, modifier = Modifier.clickable {
                            buttonLabel = result
                            showSearchDialog = false
                            onLocationSearchResultSelected(index)
                        }.padding(all = 16.dp).fillMaxWidth())
                    }
                }
            }
        }
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
                checkIn = Time.now(),
                checkOut = null,
                searchResults = List(5) { "City$it" },
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
