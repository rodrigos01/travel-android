package com.combah.travel2.ui.data

import java.util.*

data class CheckoutEvent(val hotelName: String, val checkout: Date) : TripEvent("", hotelName, checkout)