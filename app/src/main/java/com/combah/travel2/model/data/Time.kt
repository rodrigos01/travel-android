package com.combah.travel2.model.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.time.Duration

/**
 * Immutable Representation of an instant in time with a timezone
 */
data class Time(
    val timeInMillis: Long,
    val dayOfMonth: Int,
    val dayOfWeek: Int,
    val month: Int,
    val year: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val timeZone: TimeZone,
) : Comparable<Time> {

    constructor(timeInMillis: Long, timeZone: TimeZone) : this(
        Calendar.getInstance(timeZone).also { it.timeInMillis = timeInMillis })

    private constructor(calendar: Calendar) : this(
        calendar.timeInMillis,
        calendar[Calendar.DAY_OF_MONTH],
        calendar[Calendar.DAY_OF_WEEK],
        calendar[Calendar.MONTH] + 1,
        calendar[Calendar.YEAR],
        calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE],
        calendar[Calendar.SECOND],
        calendar.timeZone,
    )

    operator fun plus(other: Long): Time =
        Time(timeInMillis = timeInMillis + other, timeZone = timeZone)

    operator fun plus(other: Time): Time = this + other.timeInMillis
    operator fun plus(duration: Duration) = this + duration.inWholeMilliseconds
    operator fun minus(other: Long): Time =
        Time(timeInMillis = timeInMillis - other, timeZone = timeZone)

    operator fun minus(duration: Duration) = this - duration.inWholeMilliseconds

    override operator fun compareTo(other: Time): Int {
        return timeInMillis.compareTo(other.timeInMillis)
    }

    override fun toString(): String =
        SimpleDateFormat.getDateTimeInstance().also { it.timeZone = this.timeZone }.format(
            Date(this.timeInMillis)
        )
}
