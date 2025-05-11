package travel.vola.android.extensions

import travel.vola.android.model.data.Time
import java.text.DateFormatSymbols
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val Time.monthString: String
    get() = DateFormatSymbols.getInstance().months[monthValue - 1]

val Time.dayOfMonthString: String
    get() = dayOfMonth.toString()

fun Time.dateString(style: FormatStyle = FormatStyle.SHORT): String =
    format(DateTimeFormatter.ofLocalizedDate(style))

val Time.dateString: String
    get() = dateString()

val Time.monthAndYearString: String
    get() = format(DateTimeFormatter.ofPattern("MMMM yyyy"))

val Time.dayAndMonthString: String
    get() = format(DateTimeFormatter.ofPattern("MMM d"))

val Time.dayOfWeekString: String
    get() = format(DateTimeFormatter.ofPattern("E"))

val Time.timeString: String
    get() = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun Time.asISO8601DateString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun Time.asISO8601String(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
