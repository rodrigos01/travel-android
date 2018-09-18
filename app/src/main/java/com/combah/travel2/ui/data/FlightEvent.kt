package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import java.util.*

data class FlightEvent(val origin: Place, val destination: Place, val airport: Airport, val departure: Date) : TripEvent(destination.name, airport.name, departure, origin)