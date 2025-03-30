package travel.vola.android.extensions

import travel.vola.android.model.data.Time
import java.text.DateFormatSymbols
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TimeFormatter {
    fun dayOfMonthString(time: Time): String = time.dayOfMonthString()

    fun dayAndMonthString(time: Time): String = time.dayAndMonthString()

    fun dayOfWeekString(time: Time): String = time.dayOfWeekString()

    fun timeString(time: Time): String = time.timeString()

    fun monthString(time: Time): String =
        DateFormatSymbols.getInstance().months[time.monthValue - 1]
}

fun Time.dayOfMonthString(): String = dayOfMonth.toString()

fun Time.dateString(style: FormatStyle = FormatStyle.SHORT): String =
    format(DateTimeFormatter.ofLocalizedDate(style))

fun Time.monthAndYearString(): String = format(DateTimeFormatter.ofPattern("MMMM yyyy"))

fun Time.dayAndMonthString(): String = format(DateTimeFormatter.ofPattern("MMM d"))

fun Time.dayOfWeekString(): String =
    DateFormatSymbols.getInstance().shortWeekdays[dayOfWeek.value]

fun Time.timeString(): String = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun Time.asISO8601DateString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun Time.asISO8601String(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
