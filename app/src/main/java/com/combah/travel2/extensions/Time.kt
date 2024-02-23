package com.combah.travel2.extensions

import com.combah.travel2.model.data.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun Time(source: String): Time {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z", Locale.getDefault()).parse(source)
        ?: error("Invalid Date Format")
    val offsetString = source.split(" ").getOrNull(1) ?: "GMT"
    val timezone = TimeZone.getTimeZone("GMT$offsetString")
    return Time(date.time, timezone)
}

fun Time.update(
    dayOfMonth: Int = this.dayOfMonth,
    dayOfWeek: Int = this.dayOfWeek,
    month: Int = this.month,
    year: Int = this.year,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    timeZone: TimeZone = this.timeZone,
): Time = Calendar.getInstance(timeZone).apply {
    timeInMillis = this@update.timeInMillis
    set(Calendar.DAY_OF_MONTH, dayOfMonth)
    set(Calendar.DAY_OF_WEEK, dayOfWeek)
    set(Calendar.MONTH, month - 1)
    set(Calendar.YEAR, year)
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, second)
}.let { Time(it.timeInMillis, it.timeZone) }

fun Time.toMidnight(): Time = update(hour = 0, minute = 0, second = 0)
