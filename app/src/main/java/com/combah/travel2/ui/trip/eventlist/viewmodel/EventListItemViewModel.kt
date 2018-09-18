package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.extensions.dayOfMonthString
import com.combah.travel2.extensions.dayOfWeekString
import com.combah.travel2.extensions.timeString
import com.combah.travel2.ui.data.TripEvent
import com.combah.travel2.ui.widget.ResolvingString

open class EventListItemViewModel(private val event: TripEvent) {
    open val date: String
        get() = event.timestamp.dayOfMonthString()
    open val dayOfWeek: String
        get() = event.timestamp.dayOfWeekString()
    open val time: String
        get() = event.timestamp.timeString()
    open val icon = R.drawable.ic_flight_takeoff_black_24dp
    open val title = ResolvingString(event.name)
    open val subtitle = event.location
}