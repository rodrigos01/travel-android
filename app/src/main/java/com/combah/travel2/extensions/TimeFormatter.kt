package com.combah.travel2.extensions

import com.combah.travel2.model.data.Time
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimeFormatter(private val converter: TimeConverter = TimeConverter()) {
    fun dayOfMonthString(time: Time): String =
        time.asCalendar().get(Calendar.DAY_OF_MONTH).toString()

    fun dayAndMonthString(time: Time): String =
        SimpleDateFormat("MMM d", Locale.getDefault()).apply {
            timeZone = time.timeZone
        }.format(Date(time.timeInMillis))

    fun dayOfWeekString(time: Time): String =
        DateFormatSymbols.getInstance().weekdays[time.asCalendar().get(Calendar.DAY_OF_WEEK)]

    fun timeString(time: Time): String {
        val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).apply {
            timeZone = time.timeZone
        }
        val date = Date(time.timeInMillis)
        return formatter.format(date)
    }

    fun monthString(time: Time): String =
        DateFormatSymbols.getInstance().months[time.asCalendar().get(Calendar.MONTH)]

    private fun Time.asCalendar() = converter.asCalendar(this)
}