package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.eventlist.composable.LeadingDate
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    minimumSelectableTime: Time,
    dayOfMonth: String,
    dayOfWeek: String,
    onDateSelected: (Time) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minTimeInDeviceTimeZone =
        minimumSelectableTime.update(timeZone = TimeZone.getTimeZone("UTC")).timeInMillis
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialDisplayedMonthMillis = minTimeInDeviceTimeZone,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= minTimeInDeviceTimeZone
            }
        }
    )
    FilledTonalButton(
        onClick = { showDatePicker.value = true },
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        modifier = modifier,
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
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)
        ) {
            DatePickerButton(
                minimumSelectableTime = Time(1000L, TimeZone.getDefault()),
                dayOfMonth = "15",
                dayOfWeek = "Wed",
                onDateSelected = {},
            )
        }
    }
}
