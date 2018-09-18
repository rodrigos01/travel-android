package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Hotel

data class CheckoutEvent(val hotel: Hotel) : TripEvent("", hotel.name, hotel.checkout, hotel.place)