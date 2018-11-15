package com.combah.travel2.ui.widget

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import com.combah.travel2.extensions.createLiveData
import java.util.*

fun makeDatePickerDialog(context: Context, initialDate: Date = Date()) = createLiveData<Date> {
    val initialCalendar = Calendar.getInstance()
    initialCalendar.time = initialDate

    val dateSetListener = { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
        val resultCalendar = Calendar.getInstance()
        resultCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        resultCalendar[Calendar.MONTH] = month
        resultCalendar[Calendar.YEAR] = year
        postValue(resultCalendar.time)
    }

    DatePickerDialog(context, dateSetListener,
            initialCalendar[Calendar.YEAR],
            initialCalendar[Calendar.MONTH],
            initialCalendar[Calendar.DAY_OF_MONTH])
            .show()
}