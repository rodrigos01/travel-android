package com.combah.travel2.model.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

data class Time(
    val timeInMillis: Long,
    val timeZone: TimeZone,
) : Comparable<Time> {
    override operator fun compareTo(other: Time): Int {
        return timeInMillis.compareTo(other.timeInMillis)
    }

    operator fun plus(other: Long): Time = copy(timeInMillis = timeInMillis + other)
    operator fun minus(other: Long): Time = copy(timeInMillis = timeInMillis - other)
    operator fun plus(other: Time): Time = this + other.timeInMillis

    override fun toString(): String =
        SimpleDateFormat.getDateTimeInstance().also { it.timeZone = this.timeZone }.format(
            Date(this.timeInMillis)
        )
}