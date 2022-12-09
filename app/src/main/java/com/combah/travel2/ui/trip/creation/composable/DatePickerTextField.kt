package com.combah.travel2.ui.trip.creation.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import com.combah.travel2.extensions.dateFrom
import com.combah.travel2.extensions.format
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerTextField(
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    date: Date? = null,
    minDate: Date? = null,
    label: String? = null,
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        OutlinedTextField(
            value = date?.format() ?: "",
            onValueChange = {},
            label = { Text(text = label ?: "") },
            readOnly = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = {
                    val picker =
                        MaterialDatePicker.Builder
                            .datePicker()
                            .setCalendarConstraints(
                                CalendarConstraints
                                    .Builder()
                                    .setValidator(
                                        DateValidatorPointForward.from(
                                            minDate?.time ?: 0L
                                        )
                                    )
                                    .build()
                            )
                            .setSelection(date?.time)
                            .build()
                    picker.addOnPositiveButtonClickListener { timestamp ->
                        onDateSelected(dateFrom(timestamp, TimeZone.getTimeZone("UTC")))
                    }
                    picker.show((context as AppCompatActivity).supportFragmentManager, picker.toString())
                }),
        )
    }

}