package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.widget.ResolvingString

class ArrivalEventViewModel(event: ArrivalEvent, first: Boolean = false) : EventListItemViewModel(event, first) {
    override val icon: Int
        get() = R.drawable.ic_flight_land_black_24dp
    override val title: ResolvingString
        get() = ResolvingString(R.string.flight_arrival_tile)
}