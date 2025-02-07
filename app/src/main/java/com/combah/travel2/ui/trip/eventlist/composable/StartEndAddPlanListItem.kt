package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import com.combah.travel2.ui.trip.creation.composable.TypeSelectorButton
import java.util.TimeZone

@Composable
fun StartEndAddPlanListItem(
    initialType: AddPlanType,
    onTypeSelected: (AddPlanType) -> Unit,
    typeSelectionEnabled: Boolean,
    startTitle: @Composable () -> Unit,
    minStartTime: Time,
    startDateSelectionEnabled: Boolean = true,
    startDayOfMonth: String,
    startDayOfWeek: String,
    onStartDateChanged: (Time) -> Unit,
    startTimeSelectorLabel: String,
    startTime: String? = null,
    onStartTimeChanged: (hour: Int, minute: Int) -> Unit,
    startLabelText: String? = null,
    startPlaceHolder: String? = null,
    startText: String? = null,
    onStartTextChanged: (CharSequence) -> Unit,
    startSearchResults: List<String> = emptyList(),
    startSearchResultTapped: (Int) -> Unit,
    endTitle: @Composable () -> Unit,
    minEndTime: Time,
    endTimeSelectorLabel: String,
    endTime: String? = null,
    endDayOfMonth: String,
    endDayOfWeek: String,
    onEndDateChanged: (Time) -> Unit,
    onEndTimeChanged: (hour: Int, minute: Int) -> Unit,
    showEndTextField: Boolean = true,
    endLabelText: String? = null,
    endPlaceHolder: String? = null,
    endText: String? = null,
    onEndTextChanged: (CharSequence) -> Unit = {},
    endSearchResults: List<String> = emptyList(),
    endSearchResultTapped: (Int) -> Unit = {},
    saveButtonEnabled: Boolean = true,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
    deleteButtonEnabled: Boolean,
    onDeleteConfirmed: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            onConfirm = onDeleteConfirmed,
            onDismiss = { showDeleteConfirmation = false },
            confirmButtonLabel = "Delete",
            confirmButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            dismissButtonLabel = "Cancel"
        ) {
            Text("Delete ${initialType.label}?")
        }
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Row {
            TypeSelectorButton(
                initialType = initialType,
                onOptionSelected = onTypeSelected,
                enabled = typeSelectionEnabled,
            )
            if (deleteButtonEnabled) {
                Spacer(modifier = Modifier.weight(1F))
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text("Delete")
                }
            }
        }
        AddPlanRow(
            title = startTitle,
            minTime = minStartTime,
            timeSelectorLabel = startTimeSelectorLabel,
            time = startTime,
            onTimeChanged = onStartTimeChanged,
            dateSelectionEnabled = startDateSelectionEnabled,
            dayOfMonth = startDayOfMonth,
            dayOfWeek = startDayOfWeek,
            onDateChanged = onStartDateChanged,
            placeHolder = startPlaceHolder,
            labelText = startLabelText,
            text = startText,
            onTextChanged = onStartTextChanged,
            searchResults = startSearchResults,
            searchResultTapped = startSearchResultTapped,
        )
        AddPlanRow(
            title = endTitle,
            minTime = minEndTime,
            timeSelectorLabel = endTimeSelectorLabel,
            time = endTime,
            onTimeChanged = onEndTimeChanged,
            dayOfMonth = endDayOfMonth,
            dayOfWeek = endDayOfWeek,
            onDateChanged = onEndDateChanged,
            showTextField = showEndTextField,
            placeHolder = endPlaceHolder,
            labelText = endLabelText,
            text = endText,
            onTextChanged = onEndTextChanged,
            searchResults = endSearchResults,
            searchResultTapped = endSearchResultTapped,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp, end = 16.dp).align(Alignment.End)
        ) {
            OutlinedButton(onClick = { onCancelButtonTapped() }) {
                Text("Cancel")
            }
            Button(onClick = { onSaveButtonTapped() }, enabled = saveButtonEnabled) {
                Text("Save")
            }
        }
    }

}

@Composable
@Preview
fun StartEndAddPlanListItemPreview() {
    AppTheme {
        StartEndAddPlanListItem(
            initialType = AddPlanType.Flight,
            typeSelectionEnabled = false,
            onTypeSelected = {},
            startTitle = { Text("Start") },
            minStartTime = Time(0L, TimeZone.getDefault()),
            startTimeSelectorLabel = "Pick Start Time",
            onStartTimeChanged = { _, _ -> },
            startDateSelectionEnabled = false,
            startDayOfMonth = "14",
            startDayOfWeek = "Tue",
            onStartDateChanged = {},
            startPlaceHolder = "Enter Start point",
            startLabelText = "Start",
            onStartTextChanged = {},
            startSearchResultTapped = {},
            endTitle = { Text("End") },
            minEndTime = Time(0L, TimeZone.getDefault()),
            endTimeSelectorLabel = "Pick End Time",
            endDayOfMonth = "2",
            endDayOfWeek = "Wed",
            onEndDateChanged = {},
            endPlaceHolder = "Enter End point",
            endLabelText = "End",
            onEndTimeChanged = { _, _ -> },
            onEndTextChanged = {},
            endSearchResultTapped = {},
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
            deleteButtonEnabled = true,
            onDeleteConfirmed = {},
        )
    }
}
