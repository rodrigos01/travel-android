package com.combah.travel2.ui.widget

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import com.combah.travel2.extensions.createLiveData
import java.util.*

fun makeDatePickerDialog(context: Context, initialDate: Date? = Date(), dateSet: (Date) -> Unit) {
    val initialCalendar = Calendar.getInstance()
    initialDate?.let { initialCalendar.time = it }

    val dateSetListener = { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
        val resultCalendar = Calendar.getInstance()
        resultCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        resultCalendar[Calendar.MONTH] = month
        resultCalendar[Calendar.YEAR] = year
        dateSet(resultCalendar.time)
    }

    DatePickerDialog(context, dateSetListener,
            initialCalendar[Calendar.YEAR],
            initialCalendar[Calendar.MONTH],
            initialCalendar[Calendar.DAY_OF_MONTH])
            .show()
}