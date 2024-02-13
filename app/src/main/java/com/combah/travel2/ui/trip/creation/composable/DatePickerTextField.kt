package com.combah.travel2.ui.trip.creation.composable

import android.text.format.DateFormat.is24HourFormat
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.combah.travel2.extensions.format
import com.combah.travel2.extensions.formatTime
import com.combah.travel2.extensions.hour
import com.combah.travel2.extensions.minute
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Composable
fun DatePickerTextField(
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    date: Date? = null,
    minDate: Date? = null,
    label: String? = null,
) {
    val openDialog = remember {
        mutableStateOf(false)
    }
    val dateTimePickerState = rememberDateTimePickerState(minDate, date)
    when {
        openDialog.value -> DateTimePickerDialog(
            state = dateTimePickerState,
            onDismiss = { openDialog.value = false },
            onDateSelected = { timestamp ->
                openDialog.value = false
                onDateSelected(Date(timestamp))
            },
        )
    }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = date?.format(
                style = DateFormat.SHORT,
                includeTime = true,
                targetTimeZone = TimeZone.getTimeZone("UTC"),
            ) ?: "",
            onValueChange = {},
            label = { Text(text = label ?: "") },
            readOnly = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = { openDialog.value = true }),
        )
    }
}

@VisibleForTesting
enum class PickerStep {
    Date, Time,
}

@OptIn(ExperimentalMaterial3Api::class)
@VisibleForTesting
class DateTimePickerState(
    val minDate: Date? = null,
    val datePickerState: DatePickerState,
    val timePickerState: TimePickerState,
) {

    constructor(
        minDate: Date?,
        date: Date?,
        is24Hour: Boolean,
    ) : this(
        minDate,
        DatePickerState(
            locale = Locale.getDefault(),
            date?.time,
            minDate?.time,
            DatePickerDefaults.YearRange,
            DisplayMode.Picker,
        ),
        TimePickerState(
            initialHour = date?.hour ?: 0,
            initialMinute = date?.minute ?: 0,
            is24Hour = is24Hour,
        ),
    )

    private val stepState = mutableStateOf(PickerStep.Date)
    private val selectedDateMillis
        get() = datePickerState.selectedDateMillis

    private val selectedHour
        get() = timePickerState.hour

    private val selectedMinutes
        get() = timePickerState.minute

    var step: PickerStep
        get() = stepState.value
        set(value) {
            stepState.value = value
        }

    val selectedTimestamp: Long?
        get() {
            val dateTimestamp = selectedDateMillis ?: return null
            val hourInMillis = TimeUnit.HOURS.toMillis(selectedHour.toLong())
            val minutesInMillis = TimeUnit.MINUTES.toMillis(selectedMinutes.toLong())
            return dateTimestamp + hourInMillis + minutesInMillis
        }

    val dateConfirmEnabled: Boolean
        get() = selectedDateMillis != null

    val timeConfirmEnabled: Boolean
        get() = validateTime(minDate?.time, selectedTimestamp ?: 0L)

    val showTimeErrorMessage: Boolean
        get() = !timeConfirmEnabled
}

@Composable
private fun rememberDateTimePickerState(
    minDate: Date? = null,
    date: Date? = null,
    is24Hour: Boolean = is24HourFormat(LocalContext.current),
): DateTimePickerState = remember(minDate, date, is24Hour) {
    DateTimePickerState(
        minDate,
        date,
        is24Hour,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDialog(
    state: DateTimePickerState,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    when (state.step) {
        PickerStep.Date -> ConfirmationDialog(
            onConfirm = { state.step = PickerStep.Time },
            onDismiss = onDismiss,
            buttonEnabled = state.dateConfirmEnabled
        ) {
            DatePicker(
                state = state.datePickerState,
            )
        }

        PickerStep.Time -> ConfirmationDialog(
            onConfirm = {
                state.selectedTimestamp?.let(onDateSelected)
            },
            onDismiss = { state.step = PickerStep.Date },
            buttonEnabled = state.timeConfirmEnabled,
            errorMessage = if (state.showTimeErrorMessage) {
                state.minDate?.formatTime(
                    style = DateFormat.SHORT,
                    targetTimeZone = TimeZone.getTimeZone("UTC"),
                )
            } else {
                null
            }?.let { "Please select a time after $it" },
        ) {
            TimePicker(
                modifier = Modifier.padding(top = 16.dp),
                state = state.timePickerState,
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    buttonEnabled: Boolean,
    errorMessage: String? = null,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    content()
                }
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                TextButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.End),
                    enabled = buttonEnabled,
                    onClick = onConfirm,
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

private fun validateTime(minDateTimestamp: Long?, time: Long) =
    minDateTimestamp == null || time > minDateTimestamp

@Composable
@Preview
fun DateTimePickerDialogPreview() {
    MaterialTheme {
        val dateTimePickerState = rememberDateTimePickerState(
            minDate = Date(1700076900000),
            date = Date(1700076900000),
        )
        dateTimePickerState.step = PickerStep.Time
        DateTimePickerDialog(state = dateTimePickerState, onDismiss = {}, onDateSelected = {})
    }
}
