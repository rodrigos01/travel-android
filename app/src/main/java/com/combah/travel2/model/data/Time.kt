package com.combah.travel2.model.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class Time private constructor(
    private val calendar: Calendar,
) : Comparable<Time> {

    constructor(
        timeInMillis: Long,
        timeZone: TimeZone,
    ) : this(Calendar.getInstance().also {
        it.timeInMillis = timeInMillis
        it.timeZone = timeZone
    })

    val timeInMillis: Long
        get() = calendar.timeInMillis

    val timeZone: TimeZone
        get() = calendar.timeZone

    val dayOfMonth: Int
        get() = calendar[Calendar.DAY_OF_MONTH]

    val dayOfWeek: Int
        get() = calendar[Calendar.DAY_OF_WEEK]
    val month: Int
        get() = calendar[Calendar.MONTH]
    val year: Int
        get() = calendar[Calendar.YEAR]
    val hour: Int
        get() = calendar[Calendar.HOUR]

    val minute: Int
        get() = calendar[Calendar.MINUTE]

    val second: Int
        get() = calendar[Calendar.SECOND]

    operator fun plus(other: Long): Time = copy(timeInMillis = timeInMillis + other)
    operator fun minus(other: Long): Time = copy(timeInMillis = timeInMillis - other)
    operator fun plus(other: Time): Time = this + other.timeInMillis

    override operator fun compareTo(other: Time): Int {
        return timeInMillis.compareTo(other.timeInMillis)
    }

    fun copy(timeInMillis: Long = this.timeInMillis, timeZone: TimeZone = this.timeZone) =
        Time(timeInMillis, timeZone)

    fun copy(
        dayOfMonth: Int = this.dayOfMonth,
        month: Int = this.month,
        year: Int = this.year,
        hour: Int = this.hour,
        minute: Int = this.minute,
        second: Int = this.second,
    ): Time {
        return Calendar.getInstance(timeZone).apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
        }.asTime()
    }

    override fun toString(): String =
        SimpleDateFormat.getDateTimeInstance().also { it.timeZone = this.timeZone }.format(
            Date(this.timeInMillis)
        )

    override fun equals(other: Any?): Boolean {
        return other is Time && timeInMillis == other.timeInMillis
    }
}

private fun Calendar.asTime() = Time(timeInMillis, timeZone)