package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.widget.ResolvingString
import java.text.DateFormatSymbols

class MonthEventViewModel(event: MonthEvent) {
    val title = ResolvingString(R.string.month_event_title,
            DateFormatSymbols.getInstance().months[event.month],
            event.year)
}