package com.combah.travel2.extensions

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.timeString(style: Int = SimpleDateFormat.SHORT, locale: Locale = Locale.getDefault()): String {
    return SimpleDateFormat.getTimeInstance(style, locale).format(this)
}

fun Date.dateString(style: Int = SimpleDateFormat.SHORT, locale: Locale = Locale.getDefault()): String {
    return SimpleDateFormat.getTimeInstance(style, locale).format(this)
}

fun Date.dayOfMonthString(locale: Locale = Locale.getDefault()) = SimpleDateFormat("dd", locale)
    .format(this)

fun Date.dayOfWeekString(locale: Locale = Locale.getDefault()) = SimpleDateFormat("EEE", locale)
        .format(this)

fun Number.asString(locale: Locale = Locale.getDefault()) = NumberFormat.getInstance(locale).format(this)