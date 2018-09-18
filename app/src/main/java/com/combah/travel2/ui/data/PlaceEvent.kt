package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Place
import java.util.*

data class PlaceEvent(val currentPlace: Place, val arrival: Date) : TripEvent(currentPlace.name, currentPlace.name, arrival, currentPlace)