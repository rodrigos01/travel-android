package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Place
import java.util.*

data class MonthEvent(val month: Int, val year: Int) : TripEvent(
        "",
        "",
        Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time,
        Place()
)