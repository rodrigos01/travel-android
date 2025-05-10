package travel.vola.android.ui.trip.creation.composable

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.timeInMillis
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import java.time.ZoneId

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerDialog(
    minimumSelectableTime: Time? = null,
    selectedTime: Time? = null,
    onDateSelected: (Time) -> Unit,
    onDismiss: () -> Unit,
) {
    val minTimeInDeviceTimeZone = minimumSelectableTime?.update(timeZone = ZoneId.of("UTC"))
    val selectedTimeInDeviceTimeZone = selectedTime?.update(timeZone = ZoneId.of("UTC"))
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = selectedTimeInDeviceTimeZone?.timeInMillis,
            initialDisplayedMonthMillis = selectedTimeInDeviceTimeZone?.timeInMillis
                ?: minTimeInDeviceTimeZone?.timeInMillis ?: Time.now().timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return minTimeInDeviceTimeZone == null || utcTimeMillis >= minTimeInDeviceTimeZone.timeInMillis
                }
            })
    ConfirmationDialog(
        onConfirm = {
            datePickerState.selectedTime?.let {
                onDateSelected(
                    it
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

@Composable
@Preview
fun DatePickerButtonPreview() {
    AppTheme {
        Surface {
            DatePickerDialog(
                selectedTime = Time("2025-11-28T00:00 -0300"),
                minimumSelectableTime = Time("2025-11-29T00:00 -0300"),
                onDateSelected = {},
                onDismiss = {},
            )
        }
    }
}
