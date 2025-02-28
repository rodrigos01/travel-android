package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    minimumSelectableTime: Time?,
    onDateSelected: (Time) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val minTimeInDeviceTimeZone =
        minimumSelectableTime?.update(timeZone = TimeZone.getTimeZone("UTC"))?.timeInMillis ?: 0L
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialDisplayedMonthMillis = minTimeInDeviceTimeZone,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= minTimeInDeviceTimeZone
            }
        }
    )
    Surface(onClick = { showDatePicker.value = true }, modifier = modifier) { content() }
    if (showDatePicker.value) {
        ConfirmationDialog(
            onConfirm = {
                datePickerState.selectedTime?.let {
                    onDateSelected(
                        it
                    )
                }
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false },
            confirmButtonEnabled = true,
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

@Composable
@Preview
fun DatePickerButtonPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            DatePickerButton(
                minimumSelectableTime = Time(1000L, TimeZone.getDefault()),
                onDateSelected = {},
            ) {
                TextButton(onClick = {}) { Text("Pick Date") }
            }
        }
    }
}
