package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.ui.data.CheckoutEvent
import com.combah.travel2.ui.widget.ResolvingString

class CheckoutEventViewModel(event: CheckoutEvent, first: Boolean = false) : EventListItemViewModel(event, first) {
    override val icon: Int
        get() = R.drawable.ic_hotel_black_24dp
    override val title: ResolvingString
        get() = ResolvingString(R.string.hotel_checkout_title)
}