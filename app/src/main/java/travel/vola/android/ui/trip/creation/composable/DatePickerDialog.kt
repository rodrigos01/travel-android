package travel.vola.android.ui.trip.creation.composable

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.R
import travel.vola.android.common.ui.components.IconTextButton
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.timeInMillis
import travel.vola.android.extensions.update
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.theme.AppTheme
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

@Composable
fun DatePickerButton(
    label: String,
    selectedTime: ZonedDateTime? = null,
    minTime: ZonedDateTime? = null,
    onDateSelected: (ZonedDateTime) -> Unit,
) {
    var showDatePickerState by remember { mutableStateOf(false) }
    IconTextButton(
        onClick = {
            showDatePickerState = true
        },
        leadingIcon = ImageVector.vectorResource(R.drawable.today_baseline_24),
    ) {
        Text(selectedTime?.dateString() ?: label)
    }
    if (showDatePickerState) {
        DatePickerDialog(
            selectedTime = selectedTime,
            minimumSelectableTime = minTime,
            onDateSelected = onDateSelected,
            onDismiss = {
                showDatePickerState = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialog(
    minimumSelectableTime: ZonedDateTime? = null,
    selectedTime: ZonedDateTime? = null,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val minTimeInDeviceTimeZone = minimumSelectableTime?.update(timeZone = ZoneId.of("UTC"))
    val selectedTimeInDeviceTimeZone = selectedTime?.update(timeZone = ZoneId.of("UTC"))
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = selectedTimeInDeviceTimeZone?.timeInMillis,
            initialDisplayedMonthMillis = selectedTimeInDeviceTimeZone?.timeInMillis
                ?: minTimeInDeviceTimeZone?.timeInMillis ?: ZonedDateTime.now().timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return minTimeInDeviceTimeZone == null || utcTimeMillis >= minTimeInDeviceTimeZone.timeInMillis
                }
            },
        )
    ConfirmationDialog(
        onConfirm = {
            datePickerState.selectedTime?.let {
                onDateSelected(
                    it.update(
                        timeZone = selectedTime?.zone ?: ZoneId.systemDefault(),
                    ),
                )
            }
            onDismiss()
        },
        onDismiss = onDismiss,
        confirmButtonEnabled = true,
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}

/**
 * Currently selected date as a Time object. TimeZone is always UTC
 */
@OptIn(ExperimentalMaterial3Api::class)
val DatePickerState.selectedTime: ZonedDateTime?
    get() = selectedDateMillis?.let { zonedDateTime(it, TimeZone.getTimeZone("UTC")) }

@Composable
@Preview
fun DatePickerButtonPreview() {
    AppTheme {
        Surface {
            DatePickerDialog(
                selectedTime = zonedDateTime("2025-11-28T00:00 -0300"),
                minimumSelectableTime = zonedDateTime("2025-11-29T00:00 -0300"),
                onDateSelected = {},
                onDismiss = {},
            )
        }
    }
}
