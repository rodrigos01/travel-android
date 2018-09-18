package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.ui.data.FlightEvent
import com.combah.travel2.ui.widget.ResolvingString

class FlightEventViewModel(private val event: FlightEvent) : EventListItemViewModel(event) {
    override val icon: Int
        get() = R.drawable.ic_flight_takeoff_black_24dp
    override val title: ResolvingString
        get() = ResolvingString(R.string.flight_event_tile, event.destination.name)
}