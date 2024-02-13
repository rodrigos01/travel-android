package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.trip.eventlist.composable.LeadingDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    minimumSelectableDateMillis: Long,
    dayOfMonth: String,
    dayOfWeek: String,
    onDateSelected: (Time) -> Unit,
    modifier: Modifier,
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialDisplayedMonthMillis = minimumSelectableDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > minimumSelectableDateMillis
            }
        }
    )
    FilledTonalButton(
        onClick = { showDatePicker.value = true },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Row {
            LeadingDate(dayOfMonth = dayOfMonth, dayOfWeek = dayOfWeek)
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down_24),
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
            buttonEnabled = true,
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}
