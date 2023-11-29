package com.combah.travel2.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Date.timeString(
    style: Int = SimpleDateFormat.SHORT,
    locale: Locale = Locale.getDefault()
): String {
    return SimpleDateFormat.getTimeInstance(style, locale).format(this)
}

fun Date.dayOfMonthString(locale: Locale = Locale.getDefault()) =
    SimpleDateFormat("dd", locale).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
        .format(this)

fun Date.dayOfWeekString(locale: Locale = Locale.getDefault()) = SimpleDateFormat("EEE", locale)
    .format(this)