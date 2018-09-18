package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Hotel

data class CheckinEvent(val hotel: Hotel) : TripEvent("", hotel.name, hotel.checkin, hotel.place)