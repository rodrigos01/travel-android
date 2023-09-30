package com.combah.travel2.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun Date.asCalendar(): Calendar = Calendar.getInstance().apply { time = this@asCalendar }

fun dateFromString(value: String, format: String? = "yyyy-MM-dd'T'HH:mm"): Date? {
    return SimpleDateFormat(format, Locale.getDefault()).parse(value)
}

fun Date.formatTime(
    style: Int = DateFormat.MEDIUM,
    targetTimeZone: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale.getDefault(),
): String {
    return SimpleDateFormat.getTimeInstance(style, locale).apply {
        timeZone = targetTimeZone
    }
        .format(this)
}

fun Date.format(
    style: Int = DateFormat.MEDIUM,
    includeTime: Boolean = false,
    targetTimeZone: TimeZone = TimeZone.getDefault(),
): String {
    return if (includeTime) {
        SimpleDateFormat.getDateTimeInstance(style, style)
    } else {
        SimpleDateFormat.getDateInstance(style)
    }.apply {
        timeZone = targetTimeZone
    }
        .format(this)
}

val Date.midnightTime: Long
    get() = time - (time % TimeUnit.DAYS.toMillis(1))

private val Date.timeWithoutDays: Long
    get() = time - (time / TimeUnit.DAYS.toMillis(1))
        .let { TimeUnit.DAYS.toMillis(it) }

val Date.hour: Int
    get() = (timeWithoutDays / TimeUnit.HOURS.toMillis(1)).toInt()

val Date.minute: Int
    get() = (timeWithoutDays % TimeUnit.HOURS.toMillis(1)).let {
        TimeUnit.MILLISECONDS.toMinutes(it)
    }.toInt()