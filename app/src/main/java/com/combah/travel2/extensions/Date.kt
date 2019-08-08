package com.combah.travel2.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.asCalendar() = Calendar.getInstance().apply { time = this@asCalendar }

fun dateFromString(value: String, format: String? = "yyyy-MM-dd'T'HH:mm"): Date {
    return SimpleDateFormat(format, Locale.getDefault()).parse(value)
}

fun Date.format(style: Int = DateFormat.MEDIUM): String {
    return SimpleDateFormat.getDateInstance(style).format(this)
}