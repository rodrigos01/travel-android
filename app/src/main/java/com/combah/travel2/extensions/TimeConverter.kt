package com.combah.travel2.extensions

import com.combah.travel2.model.data.Time
import java.util.Calendar

class TimeConverter {
    fun asCalendar(time: Time): Calendar = Calendar.getInstance().also {
        it.timeInMillis = time.timeInMillis
        it.timeZone = time.timeZone
    }
}