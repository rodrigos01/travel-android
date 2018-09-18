package com.combah.travel2.ui.data

import java.util.*

data class CheckinEvent(val hotelName: String, val checkin: Date) : TripEvent("", hotelName, checkin)