package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Airport
import java.util.*

data class ArrivalEvent(val airport: Airport, val arrival: Date) : TripEvent("", airport.name, arrival)