package com.combah.travel2.extensions

import com.combah.travel2.model.data.Time
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun Time(source: String): Time {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z", Locale.getDefault()).parse(source)
        ?: error("Invalid Date Format")
    val offsetString = source.split(" ").getOrNull(1) ?: "GMT"
    val timezone = TimeZone.getTimeZone("GMT$offsetString")
    return Time(date.time, timezone)
}
