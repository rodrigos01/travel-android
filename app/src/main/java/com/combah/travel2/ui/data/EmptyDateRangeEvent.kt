package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Place
import java.util.Date

data class EmptyDateRangeEvent(val dateStart: Date, val dateEnd: Date) :
    TripEvent("", "", dateStart, Place())