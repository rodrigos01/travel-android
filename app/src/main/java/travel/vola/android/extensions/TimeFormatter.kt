package travel.vola.android.extensions

import java.text.DateFormatSymbols
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val ZonedDateTime.monthString: String
    get() = DateFormatSymbols.getInstance().months[monthValue - 1]

val ZonedDateTime.dayOfMonthString: String
    get() = dayOfMonth.toString()

fun ZonedDateTime.dateString(format: String): String =
    format(DateTimeFormatter.ofPattern(format))

fun ZonedDateTime.dateString(style: FormatStyle = FormatStyle.SHORT): String =
    format(DateTimeFormatter.ofLocalizedDate(style))

val ZonedDateTime.dateString: String
    get() = dateString()

val ZonedDateTime.dateTimeString: String
    get() = format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))

val ZonedDateTime.monthAndYearString: String
    get() = format(DateTimeFormatter.ofPattern("MMMM yyyy"))

val ZonedDateTime.dayAndMonthString: String
    get() = format(DateTimeFormatter.ofPattern("MMM d"))

val ZonedDateTime.dayOfWeekString: String
    get() = format(DateTimeFormatter.ofPattern("E"))

val ZonedDateTime.timeString: String
    get() = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun ZonedDateTime.asISO8601DateString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun ZonedDateTime.asISO8601String(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
