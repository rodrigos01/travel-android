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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.combah.travel2.extensions.dayOfMonthString
import com.combah.travel2.extensions.dayOfWeekString
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.timeString
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

data class AddPlanRowState<Result>(
    var selectedTime: Time?,
    val minTime: Time,
    val searchResults: List<Result>,
    var selectedSearchResultIndex: Int,
)

@Composable
fun <Result> rememberAddPlanRowState(
    selectedTime: Time? = null,
    minTime: Time = Time.now(),
    searchResults: List<Result> = emptyList(),
    selectedSearchResultIndex: Int = -1,
) = remember { AddPlanRowState(selectedTime, minTime, searchResults, selectedSearchResultIndex) }

@Composable
fun <T> AddPlanRow(
    title: @Composable () -> Unit,
    state: AddPlanRowState<T>,
    timeSelectorLabel: String,
    text: String? = null,
    dateSelectionEnabled: Boolean = true,
    showTextField: Boolean = true,
    labelText: String? = null,
    placeHolder: String? = null,
    onTextChanged: (CharSequence) -> Unit = {},
    searchResultItemContent: (T) -> String,
) {
    val selectedTime = state.selectedTime ?: state.minTime
    val isMinDate = selectedTime.toMidnight() == state.minTime.toMidnight()
    val timePickerDialogState = rememberTimePickerDialogState(
        minHour = if (isMinDate) state.minTime.hour else 0,
        minMinute = if (isMinDate) state.minTime.minute else 0,
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
                    text = state.selectedTime?.timeString() ?: timeSelectorLabel,
                    onTimeSelected = { hour, minute ->
                        state.selectedTime =
                            selectedTime.update(hour = hour, minute = minute)
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
                    state.minTime.toMidnight(),
                    onDateSelected = {
                        state.selectedTime = selectedTime.update(
                            dayOfMonth = it.dayOfMonth,
                            month = it.month,
                            year = it.year,
                        )
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
                            LeadingDate(
                                dayOfMonth = selectedTime.dayOfMonthString(),
                                dayOfWeek = selectedTime.dayOfWeekString(),
                            )
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
                    dayOfMonth = selectedTime.dayOfMonthString(),
                    dayOfWeek = selectedTime.dayOfWeekString(),
                    modifier = Modifier.width(80.dp),
                )
            }
            if (showTextField) {
                AutoCompleteTextField(
                    state = rememberAutoCompleteTextFieldState(
                        text, state.searchResults,
                    ),
                    label = labelText,
                    placeHolder = placeHolder,
                    onTextChanged,
                    onOptionSelected = { state.selectedSearchResultIndex = it },
                    itemContent = searchResultItemContent,
                )
            } else {
                val showTimePicker = remember {
                    mutableStateOf(false)
                }
                val focusManager = LocalFocusManager.current
                TimePickerButton(
                    onTimeSelected = { hour, minute ->
                        state.selectedTime = selectedTime.update(hour = hour, minute = minute)
                        focusManager.clearFocus()
                    },
                    showTimePickerState = showTimePicker,
                    timePickerDialogState = timePickerDialogState,
                ) {
                    OutlinedTextField(
                        value = selectedTime.timeString(),
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
                state = rememberAddPlanRowState<String>(
                    selectedTime = Time.now(),
                    minTime = Time(0L, TimeZone.getDefault()),
                    searchResults = emptyList(),
                ),
                title = { Text("Title") },
                timeSelectorLabel = "Pick Time",
                dateSelectionEnabled = true,
                showTextField = true,
                placeHolder = "PlaceHolder",
                labelText = "Label",
                onTextChanged = {},
                searchResultItemContent = { it },
            )
        }
    }
}
