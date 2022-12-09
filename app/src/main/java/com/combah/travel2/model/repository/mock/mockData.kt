package com.combah.travel2.model.repository.mock

import com.combah.travel2.extensions.dateFromString
import com.combah.travel2.model.data.*
import java.util.*

object MockData {
        val jfk = Airport("JFK", "JFK International Airport")
        val nyc = Place(name = "New York")
        val cdg = Airport("CDG", "Charles de Gaule International Airport")
        val paris = Place(name = "Paris")
        val flightToParisDepartureDate = dateFromString("2018-10-24T10:25") ?: Date()
        val flightToParisArrivalDate = dateFromString("2018-10-25T05:15") ?: Date()
        val parisHotelName = "Hôtel Europe Saint-Séverin"
        val parisHotelCheckinDate = dateFromString("2018-10-25T13:00") ?: Date()
        val parisHotelCheckoutDate = dateFromString("2018-11-01T12:00") ?: Date()
        val parisHotel = Hotel(
                name = parisHotelName,
                place = paris,
                checkin = parisHotelCheckinDate,
                checkout = parisHotelCheckoutDate
        )
        val bru = Airport("BRU", "Brussels Airport (BRU)")
        val brussels = Place(name = "Bruxels")
        val flightToBruxelsDepartureDate = dateFromString("2018-11-01T15:00") ?: Date()
        val flightToBruxelsArrivalDate = dateFromString("2018-11-01T19:10") ?: Date()
        val trip = Trip(
                id = "minhaTrip",
                flights = listOf(
                        Flight(
                                segments = listOf(
                                        FlightSegment(
                                                airportFrom = jfk,
                                                cityFrom = nyc,
                                                airportTo = cdg,
                                                cityTo = paris,
                                                departure = flightToParisDepartureDate,
                                                arrival = flightToParisArrivalDate
                                        )
                                )
                        ),
                        Flight(
                                segments = listOf(
                                        FlightSegment(
                                                airportFrom = cdg,
                                                cityFrom = paris,
                                                airportTo = bru,
                                                cityTo = brussels,
                                                departure = flightToBruxelsDepartureDate,
                                                arrival = flightToBruxelsArrivalDate
                                        )
                                )
                        )
                ),
                hotels = listOf(parisHotel)
        )

        val tripList = listOf(trip)
}