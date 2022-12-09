package com.combah.travel2.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.asCalendar(): Calendar = Calendar.getInstance().apply { time = this@asCalendar }

fun dateFromString(value: String, format: String? = "yyyy-MM-dd'T'HH:mm"): Date? {
    return SimpleDateFormat(format, Locale.getDefault()).parse(value)
}

fun Date.format(style: Int = DateFormat.MEDIUM): String {
    return SimpleDateFormat.getDateInstance(style).format(this)
}

fun dateFrom(timestamp: Long, originTimezone: TimeZone, targetTimeZone: TimeZone = TimeZone.getDefault()): Date {
    val originCalendar = Calendar.getInstance(originTimezone)
    originCalendar.timeInMillis = timestamp
    val targetCalendar = Calendar.getInstance(targetTimeZone)
    targetCalendar.set(
        originCalendar.get(Calendar.YEAR),
        originCalendar.get(Calendar.MONTH),
        originCalendar.get(Calendar.DAY_OF_MONTH),
    )
    return targetCalendar.time
}