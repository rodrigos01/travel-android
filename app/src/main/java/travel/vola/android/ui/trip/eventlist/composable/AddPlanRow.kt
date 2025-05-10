package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.common.ui.components.Clock
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.timeString
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AutoCompleteTextField
import travel.vola.android.ui.trip.creation.composable.DatePickerDialog
import travel.vola.android.ui.trip.creation.composable.TimePickerDialog
import travel.vola.android.ui.trip.creation.composable.rememberAutoCompleteTextFieldState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.util.TimeZone

class AddPlanRowState(
    selectedTimeState: MutableState<Time?>,
    selectedSearchResultIndexState: MutableIntState,
    timeSelectedState: MutableState<Boolean>,
) {
    var selectedDateTime: Time? by selectedTimeState
    var selectedSearchResultIndex: Int by selectedSearchResultIndexState
    var timeSelected: Boolean by timeSelectedState
}

@Composable
fun rememberAddPlanRowState(
    key: Any? = null,
    selectedDateTime: Time? = null,
    selectedSearchResultIndex: Int = -1,
    timeSelected: Boolean = false,
) = remember(key, selectedDateTime) {
    AddPlanRowState(
        mutableStateOf(selectedDateTime),
        mutableIntStateOf(selectedSearchResultIndex),
        mutableStateOf(timeSelected),
    )
}

@Composable
fun AddPlanRow(
    state: AddPlanRowState,
    minTime: Time? = null,
    searchResults: List<AutoCompleteResultState>,
    title: @Composable () -> Unit,
    timeSelectorLabel: String,
    text: String? = null,
    dateSelectionEnabled: Boolean = true,
    showTextField: Boolean = true,
    labelText: String? = null,
    placeHolder: String? = null,
    onTextChanged: (CharSequence) -> Unit = {},
) {
    val selectedTime = state.selectedDateTime ?: minTime ?: Time.now()
    var showTimePicker by remember {
        mutableStateOf(false)
    }
    val timeText =
        state.selectedDateTime?.timeString?.takeIf { state.timeSelected } ?: timeSelectorLabel
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            Box(
                modifier = Modifier.width(80.dp)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium, title)
            }
            if (showTextField) {
                IconTextButton(
                    onClick = { showTimePicker = true },
                    leadingIcon = Icons.Outlined.Clock,
                    leadingIconSize = 16.dp,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    Text(
                        text = timeText, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (dateSelectionEnabled) {
                var showDatePickerState by remember { mutableStateOf(false) }
                FilledTonalButton(
                    onClick = { showDatePickerState = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 4.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
                    modifier = Modifier.width(80.dp)
                ) {
                    Row {
                        LeadingDate(
                            dayOfMonth = selectedTime.dayOfMonthString,
                            dayOfWeek = selectedTime.dayOfWeekString,
                        )
                        Image(
                            painter = painterResource(id = R.drawable.arrow_drop_down_filled_24),
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                if (showDatePickerState) {
                    DatePickerDialog(
                        minimumSelectableTime = minTime?.toMidnight(),
                        onDateSelected = {
                            state.selectedDateTime = selectedTime.update(
                                dayOfMonth = it.dayOfMonth,
                                month = it.month,
                                year = it.year,
                            )
                            showDatePickerState = false
                        },
                        selectedTime = state.selectedDateTime,
                        onDismiss = {
                            showDatePickerState = false
                        },
                    )
                }
            } else {
                LeadingDate(
                    dayOfMonth = selectedTime.dayOfMonthString,
                    dayOfWeek = selectedTime.dayOfWeekString,
                    modifier = Modifier.width(80.dp),
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
                    onOptionSelected = { state.selectedSearchResultIndex = it },
                    itemText = { it.title },
                    itemContent = { result ->
                        Column {
                            Text(result.title)
                            result.subtitle?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically),
                )
            } else {
                IconTextButton(
                    onClick = { showTimePicker = true },
                    leadingIcon = Icons.Outlined.Clock,
                ) {
                    Text(timeText)
                }
            }
        }
    }
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                state.timeSelected = true
                state.selectedDateTime = selectedTime.update(hour = hour, minute = minute)
                showTimePicker = false
            },
            initialTime = selectedTime,
            minimumSelectableTime = minTime,
            onDismissRequest = {
                showTimePicker = false
            },
        )
    }
}

@Preview
@Composable
fun AddPlanRowPreview(hasTextField: Boolean = true) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
        ) {
            val state = rememberAddPlanRowState(
                selectedDateTime = Time("2025-06-12T03:45 -0300"),
            )
            AddPlanRow(
                state = state,
                minTime = Time(0L, TimeZone.getDefault()),
                searchResults = emptyList(),
                title = { Text("Title") },
                timeSelectorLabel = "Pick Time",
                dateSelectionEnabled = true,
                showTextField = hasTextField,
                placeHolder = "PlaceHolder",
                labelText = "Label",
                onTextChanged = {},
            )
        }
    }
}

@Preview
@Composable
fun AddPlanRowPreviewNoTextField() {
    AddPlanRowPreview(hasTextField = false)
}
