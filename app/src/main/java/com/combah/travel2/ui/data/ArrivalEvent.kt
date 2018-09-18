package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import java.util.*

data class ArrivalEvent(val airport: Airport, val arrival: Date, val destination: Place) : TripEvent("", airport.name, arrival, destination)