package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AutoCompleteTextField
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton
import com.combah.travel2.ui.trip.creation.composable.TimePickerButton
import com.combah.travel2.ui.trip.creation.composable.TimePickerTextButton
import com.combah.travel2.ui.trip.creation.composable.rememberAutoCompleteTextFieldState
import com.combah.travel2.ui.trip.creation.composable.rememberTimePickerDialogState
import java.util.TimeZone

@Composable
fun AddPlanRow(
    title: @Composable () -> Unit,
    minTime: Time,
    dateSelectionEnabled: Boolean = true,
    dayOfMonth: String,
    dayOfWeek: String,
    onDateChanged: (Time) -> Unit,
    timeSelectorLabel: String,
    time: String? = null,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    showTextField: Boolean = true,
    labelText: String? = null,
    placeHolder: String? = null,
    text: String? = null,
    onTextChanged: (CharSequence) -> Unit = {},
    searchResults: List<String> = emptyList(),
    searchResultTapped: (Int) -> Unit = {},
) {
    var selectedTime by remember { mutableStateOf(minTime) }
    selectedTime = selectedTime.update(timeZone = minTime.timeZone)
    val isMinDate = selectedTime.toMidnight() == minTime.toMidnight()
    val timePickerDialogState = rememberTimePickerDialogState(
        minHour = if (isMinDate) minTime.hour else 0,
        minMinute = if (isMinDate) minTime.minute else 0,
        hour = selectedTime.hour,
        minute = selectedTime.minute,
    )
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            Box(
                modifier =
                Modifier.width(80.dp)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium, title)
            }
            if (showTextField) {
                TimePickerTextButton(
                    text = time ?: timeSelectorLabel,
                    onTimeSelected = { hour, minute ->
                        selectedTime = selectedTime.update(hour = hour, minute = minute)
                        onTimeChanged(hour, minute)
                    },
                    modifier = Modifier
                        .semantics { role = Role.Button },
                    timePickerDialogState = timePickerDialogState,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (dateSelectionEnabled) {
                DatePickerButton(
                    minTime.toMidnight(),
                    onDateSelected = {
                        selectedTime = selectedTime.update(
                            dayOfMonth = it.dayOfMonth,
                            month = it.month,
                            year = it.year,
                        )
                        onDateChanged(it)
                    },
                    modifier = Modifier.width(80.dp),
                ) {
                    FilledTonalButton(
                        onClick = {},
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 4.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                    ) {
                        Row {
                            LeadingDate(dayOfMonth = dayOfMonth, dayOfWeek = dayOfWeek)
                            Image(
                                painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
                                colorFilter = ColorFilter.tint(LocalContentColor.current),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            } else {
                LeadingDate(
                    dayOfMonth, dayOfWeek, modifier = Modifier.width(80.dp),
                )
            }
            if (showTextField) {
                AutoCompleteTextField(
                    state = rememberAutoCompleteTextFieldState(
                        text, searchResults,
                    ),
                    label = labelText,
                    placeHolder = placeHolder,
                    onTextChanged,
                    searchResultTapped,
                )
            } else {
                val showTimePicker = remember {
                    mutableStateOf(false)
                }
                val focusManager = LocalFocusManager.current
                TimePickerButton(
                    onTimeSelected = { hour, minute ->
                        selectedTime = selectedTime.update(hour = hour, minute = minute)
                        onTimeChanged(hour, minute)
                        focusManager.clearFocus()
                    },
                    showTimePickerState = showTimePicker,
                    timePickerDialogState = timePickerDialogState,
                ) {
                    OutlinedTextField(
                        value = time.orEmpty(),
                        label = { Text(timeSelectorLabel) },
                        placeholder = { placeHolder?.let { Text(it) } },
                        onValueChange = {},
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                showTimePicker.value = true
                            }
                        }
                    )
                }
            }
        }
    }

}

@Preview
@Composable
fun AddPlanRowPreview() {
    AppTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            AddPlanRow(
                title = { Text("Title") },
                minTime = Time(0L, TimeZone.getDefault()),
                timeSelectorLabel = "Pick Time",
                onTimeChanged = { _, _ -> },
                dateSelectionEnabled = true,
                dayOfMonth = "14",
                dayOfWeek = "Tue",
                onDateChanged = {},
                showTextField = true,
                placeHolder = "PlaceHolder",
                labelText = "Label",
                onTextChanged = {},
                searchResultTapped = {},
            )
        }
    }
}
