package travel.vola.android.ui.trip.creation.composable

import android.text.format.DateFormat.is24HourFormat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.extensions.hoursToMillis
import travel.vola.android.extensions.minutesToMillis
import travel.vola.android.extensions.update
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@Composable
fun DateTimePickerDialog(
    minimumSelectableTime: ZonedDateTime? = null,
    selectedTime: ZonedDateTime = ZonedDateTime.now(),
    onDateTimeSelected: (ZonedDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDateTimePickerState(
        initialDateTime = selectedTime
    )
    when (state.step) {
        PickerStep.Date -> DatePickerDialog(
            selectedTime = state.selectedDateTime,
            minimumSelectableTime = minimumSelectableTime,
            onDateSelected = { selectedDateTime ->
                state.selectedDateTime = state.selectedDateTime.update(
                    dayOfMonth = selectedDateTime.dayOfMonth,
                    month = selectedDateTime.month,
                    year = selectedDateTime.year
                )
                state.step = PickerStep.Time
            },
            onDismiss = onDismiss
        )

        PickerStep.Time -> TimePickerDialog(
            initialTime = state.selectedDateTime,
            onTimeSelected = { hour, minute ->
                state.selectedDateTime = state.selectedDateTime.update(
                    hour = hour, minute = minute
                ).also(onDateTimeSelected)
            },
            onDismissRequest = { state.step = PickerStep.Date },
            minimumSelectableTime = minimumSelectableTime,
        )
    }
}

enum class PickerStep {
    Date, Time,
}

@OptIn(ExperimentalMaterial3Api::class)
class TimePickerDialogState(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    minHour: Int = 0,
    minMinute: Int = 0,
) {
    private val minTimeMillis = minHour.hoursToMillis() + minMinute.minutesToMillis()

    val timePickerState = TimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
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

//@OptIn(ExperimentalMaterial3Api::class)
//class DatePickerDialogState(
//    date: Time?, minDate: Time?
//) {
//    private val minDateMillis = minDate?.time ?: 0L
//
//    val datePickerState = DatePickerState(
//        locale = Locale.getDefault(),
//        date?.time,
//        minDate?.time,
//        DatePickerDefaults.YearRange,
//        DisplayMode.Picker,
//    )
//
//    val selectedDateMillis
//        get() = datePickerState.selectedDateMillis
//
//
//    val dateConfirmEnabled: Boolean
//        get() = selectedDateMillis?.let { it >= minDateMillis } ?: false
//}

class DateTimePickerDialogState(
    initialDateTime: ZonedDateTime,
) {
    private val stepState = mutableStateOf(PickerStep.Date)
    private val dateTimeState = mutableStateOf(initialDateTime)

    var step: PickerStep
        get() = stepState.value
        set(value) {
            stepState.value = value
        }

    var selectedDateTime: ZonedDateTime
        get() = dateTimeState.value
        internal set(value) {
            dateTimeState.value = value
        }
}

@Composable
fun rememberDateTimePickerState(
    initialDateTime: ZonedDateTime = ZonedDateTime.now(),
): DateTimePickerDialogState = remember(initialDateTime) {
    DateTimePickerDialogState(initialDateTime)
}

@Composable
fun rememberTimePickerDialogState(
    hour: Int = 0, minute: Int = 0,
    is24Hour: Boolean = is24HourFormat(
        LocalContext.current
    ),
    minHour: Int = 0, minMinute: Int = 0,
): TimePickerDialogState = remember(hour, minute, is24Hour, minHour, minMinute) {
    TimePickerDialogState(hour, minute, is24Hour, minHour, minMinute)
}

@Composable
@Preview
fun DateTimePickerDialogPreview() {
    MaterialTheme {
        val dateTimePickerState = rememberDateTimePickerState(
            initialDateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(1700076900000),
                ZoneId.systemDefault(),
            ),
        )
        dateTimePickerState.step = PickerStep.Time
        DateTimePickerDialog(
            minimumSelectableTime = zonedDateTime(1700076960000, ZoneId.systemDefault()),
            onDismiss = {},
            onDateTimeSelected = {},
        )
    }
}
