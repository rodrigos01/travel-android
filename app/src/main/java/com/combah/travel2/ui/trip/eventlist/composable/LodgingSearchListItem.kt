package com.combah.travel2.ui.trip.eventlist.composable

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
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.common.components.IconTextButton
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LodgingSearchListItem(
    onTypeSelected: (AddPlanType) -> Unit,
    locationText: String?,
    isEditing: Boolean = false,
    onSwitchToManualButtonTapped: () -> Unit,
    onDeleteConfirmed: () -> Unit,
) {
    AddPlanScaffold(
        type = AddPlanType.Lodging,
        onTypeSelected = onTypeSelected,
        typeSelectionEnabled = !isEditing,
        deleteButtonEnabled = isEditing,
        onDeleteConfirmed = onDeleteConfirmed,
        primaryButtonEnabled = true,
        primaryButtonLabel = "Search",
        onPrimaryButtonTapped = {},
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonTapped = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            DatePickerButton(
                minimumSelectableTime = Time.now(),
                onDateSelected = {},
                modifier = Modifier.weight(1F),
            ) {
                IconTextButton(
                    onClick = {},
                    leadingIconResId = R.drawable.baseline_today_24,
                ) {
                    Text("check-in date")
                }
            }
            DatePickerButton(
                minimumSelectableTime = Time.now(),
                onDateSelected = {},
                modifier = Modifier.weight(1F),
            ) {
                IconTextButton(
                    onClick = {},
                    leadingIconResId = R.drawable.baseline_today_24,
                ) {
                    Text("check-out date")
                }
            }
        }
        var showSearchDialog by remember { mutableStateOf(false) }
        FilledTonalButton(
            onClick = {
                showSearchDialog = true
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(locationText ?: "Tap to enter city")
        }
        if (showSearchDialog) {
            Dialog(onDismissRequest = {
                showSearchDialog = false
            }) {
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = "",
                            placeholder = { Text("Enter city") },
                            onQueryChange = {},
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
                ) { }
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
        LodgingSearchListItem(
            onTypeSelected = {},
            locationText = "Paris, France",
            isEditing = false,
            onSwitchToManualButtonTapped = {},
            onDeleteConfirmed = {},
        )
    }
}