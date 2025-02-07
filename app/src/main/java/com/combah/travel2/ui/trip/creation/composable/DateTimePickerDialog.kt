package com.combah.travel2.ui.trip.creation.composable

import android.text.format.DateFormat.is24HourFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.extensions.formatTime
import com.combah.travel2.extensions.hour
import com.combah.travel2.extensions.hoursToMillis
import com.combah.travel2.extensions.minute
import com.combah.travel2.extensions.minutesToMillis
import com.combah.travel2.model.data.Time
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Composable
fun DateTimePickerDialog(
    state: DateTimePickerDialogState,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    when (state.step) {
        PickerStep.Date -> DatePickerDialog(
            state.datePickerState,
            onConfirm = { state.step = PickerStep.Time },
            onDismiss = onDismiss
        )

        PickerStep.Time -> TimePickerDialog(
            state.timePickerState,
            onConfirm = {
                state.selectedTimestamp?.let(onDateSelected)
            },
            onDismiss = { state.step = PickerStep.Date },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    state: DatePickerDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmButtonEnabled = state.dateConfirmEnabled
    ) {
        DatePicker(
            state = state.datePickerState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    state: TimePickerDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmButtonEnabled = state.timeConfirmEnabled,
    ) {
        Column {
            TimePicker(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                state = state.timePickerState,
            )
            if (state.showTimeErrorMessage) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = state.minDate.formatTime(
                            style = DateFormat.SHORT,
                            targetTimeZone = TimeZone.getTimeZone("UTC"),
                        ).let { "Please select a time after $it" },
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

enum class PickerStep {
    Date, Time,
}

@OptIn(ExperimentalMaterial3Api::class)
class TimePickerDialogState(
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    minHour: Int = 0,
    minMinute: Int = 0,
) {
    private val minTimeMillis = minHour.hoursToMillis() + minMinute.minutesToMillis()

    val timePickerState = TimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = is24Hour,
    )

    val selectedHour
        get() = timePickerState.hour

    val selectedMinutes
        get() = timePickerState.minute

    val timeConfirmEnabled: Boolean
        get() = (selectedHour.hoursToMillis() + selectedMinutes.minutesToMillis()) >= minTimeMillis

    val showTimeErrorMessage: Boolean
        get() = !timeConfirmEnabled

    val minDate = Date(minTimeMillis)
}

@OptIn(ExperimentalMaterial3Api::class)
class DatePickerDialogState(
    date: Date?, minDate: Date?
) {
    private val minDateMillis = minDate?.time ?: 0L

    val datePickerState = DatePickerState(
        locale = Locale.getDefault(),
        date?.time,
        minDate?.time,
        DatePickerDefaults.YearRange,
        DisplayMode.Picker,
    )

    val selectedDateMillis
        get() = datePickerState.selectedDateMillis


    val dateConfirmEnabled: Boolean
        get() = selectedDateMillis?.let { it >= minDateMillis } ?: false
}

class DateTimePickerDialogState(
    date: Date?,
    minDate: Date?,
    is24Hour: Boolean,
) {
    val datePickerState = DatePickerDialogState(date, minDate)
    val timePickerState = TimePickerDialogState(
        hour = date?.hour ?: 0,
        minute = date?.minute ?: 0,
        is24Hour = is24Hour,
        minHour = minDate?.hour ?: 0,
        minMinute = minDate?.minute ?: 0,
    )

    private val stepState = mutableStateOf(PickerStep.Date)

    var step: PickerStep
        get() = stepState.value
        set(value) {
            stepState.value = value
        }

    val selectedTimestamp: Long?
        get() {
            val dateTimestamp = datePickerState.selectedDateMillis ?: return null
            val hourInMillis = TimeUnit.HOURS.toMillis(timePickerState.selectedHour.toLong())
            val minutesInMillis =
                TimeUnit.MINUTES.toMillis(timePickerState.selectedMinutes.toLong())
            return dateTimestamp + hourInMillis + minutesInMillis
        }
}

/**
 * Currently selected date as a Time object. TimeZone is always UTC
 */
@OptIn(ExperimentalMaterial3Api::class)
val DatePickerState.selectedTime: Time?
    get() = selectedDateMillis?.let { Time(it, TimeZone.getTimeZone("UTC")) }

@Composable
fun rememberDateTimePickerState(
    date: Date? = null,
    minDate: Date? = null,
    is24Hour: Boolean = is24HourFormat(LocalContext.current),
): DateTimePickerDialogState = remember(minDate, date, is24Hour) {
    DateTimePickerDialogState(
        date,
        minDate,
        is24Hour,
    )
}

@Composable
fun rememberTimePickerDialogState(
    hour: Int = 0, minute: Int = 0, is24Hour: Boolean = is24HourFormat(
        LocalContext.current
    ), minHour: Int = 0, minMinute: Int = 0
): TimePickerDialogState = remember(hour, minute, is24Hour, minHour, minMinute) {
    TimePickerDialogState(hour, minute, is24Hour, minHour, minMinute)
}

@Composable
@Preview
fun DateTimePickerDialogPreview() {
    MaterialTheme {
        val dateTimePickerState = rememberDateTimePickerState(
            date = Date(1700076900000),
            minDate = Date(1700076960000),
        )
        dateTimePickerState.step = PickerStep.Time
        DateTimePickerDialog(state = dateTimePickerState, onDismiss = {}, onDateSelected = {})
    }
}
