package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Airport
import java.util.*

class ArrivalEvent(val airport: Airport, timestamp: Date) : TripEvent("", airport.name, timestamp)