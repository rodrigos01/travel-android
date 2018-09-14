package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import java.util.*

class FlightEvent(val destination: Place, val airport: Airport, timestamp: Date) : TripEvent(destination.name, airport.name, timestamp)