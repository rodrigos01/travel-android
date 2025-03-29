package travel.vola.android.extensions

import travel.vola.android.model.data.Time
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeFormatter {
    fun dayOfMonthString(time: Time): String = time.dayOfMonthString()

    fun dayAndMonthString(time: Time): String = time.dayAndMonthString()

    fun dayOfWeekString(time: Time): String = time.dayOfWeekString()

    fun timeString(time: Time): String = time.timeString()

    fun monthString(time: Time): String = DateFormatSymbols.getInstance().months[time.month - 1]
}

fun Time.dayOfMonthString(): String = dayOfMonth.toString()

fun Time.dateString(): String =
    SimpleDateFormat.getDateInstance(DateFormat.SHORT).apply { timeZone = this@dateString.timeZone }
        .format(Date(timeInMillis))

fun Time.dayAndMonthString(): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).apply {
        timeZone = this@dayAndMonthString.timeZone
    }.format(Date(timeInMillis))

fun Time.dayOfWeekString(): String =
    DateFormatSymbols.getInstance().shortWeekdays[dayOfWeek]

fun Time.timeString(): String {
    val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).apply {
        timeZone = this@timeString.timeZone
    }
    val date = Date(timeInMillis)
    return formatter.format(date)
}

fun Time.asISO8601DateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).also {
        it.timeZone = timeZone
    }.format(Date(timeInMillis))

fun Time.asISO8601String(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z", Locale.getDefault()).also {
        it.timeZone = timeZone
    }.format(Date(timeInMillis))
