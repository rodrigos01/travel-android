package com.combah.travel2.model.repository.mock

import com.combah.travel2.model.firebase.FirebaseData.Airport
import com.combah.travel2.model.firebase.FirebaseData.Flight
import com.combah.travel2.model.firebase.FirebaseData.FlightSegment
import com.combah.travel2.model.firebase.FirebaseData.Lodging
import com.combah.travel2.model.firebase.FirebaseData.Place
import com.combah.travel2.model.firebase.FirebaseData.Trip

private const val EAST_DAYLIGHT = "GMT-04:00"
private const val WESTERN_EUROPEAN_SUMMER = "GMT+01:00"
private const val CENTRAL_EUROPEAN_SUMMER = "GMT+02:00"

object MockData {
    val nyc = Place(name = "New York", timeZone = EAST_DAYLIGHT)
    val jfk = Airport("JFK", "JFK International Airport", city = nyc)
    val lisbon = Place(name = "Lisbon", timeZone = WESTERN_EUROPEAN_SUMMER)
    val lis = Airport("LIS", "Humberto Delgado International Airport", city = lisbon)
    val flightToLisbon = Flight(
        FlightSegment(
            airportFrom = jfk,
            airportTo = lis,
            departure = "2024-05-10T22:05 -0400",
            arrival = "2024-05-11T10:00 +0100",
        )
    )
    val lisbonAirBnB = Lodging(
        name = null,
        address = "R. Prof. Branco Rodrigues 2, 1200-422 Lisboa, Portugal",
        city = lisbon,
        checkIn = "2024-05-11T13:00 +0100",
        checkout = "2024-05-19T11:00 +0100",
    )
    val porto = Place(name = "Porto", timeZone = WESTERN_EUROPEAN_SUMMER)
    val opo = Airport("OPO", "Francisco Sá Carneiro Airport", city = porto)
    val portoHotel = Lodging(
        name = "Pestana Porto - A Brasileira",
        address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
        city = porto,
        checkIn = "2024-05-19T13:00 +0100",
        checkout = "2024-05-21T11:00 +0100",
    )
    val paris = Place(name = "Paris", timeZone = CENTRAL_EUROPEAN_SUMMER)
    val ory = Airport("ORY", "Orly International Airport", city = paris)
    val flightToParis = Flight(
        FlightSegment(
            airportFrom = opo,
            airportTo = ory,
            departure = "2024-05-21T16:50 +0100",
            arrival = "2024-05-21T20:15 +0200",
        )
    )
    val parisAirBnB = Lodging(
        name = null,
        address = "155 Rue du Faubourg Saint-Antoine, 75011 Paris, France",
        city = paris,
        checkIn = "2024-05-21T13:00 +0200",
        checkout = "2024-05-29T11:00 +0200",
    )
    val nice = Place(name = "Nice", timeZone = CENTRAL_EUROPEAN_SUMMER)
    val niceHotel = Lodging(
        name = "Hôtel La Villa Nice Victor Hugo",
        address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
        city = nice,
        checkIn = "2024-05-29T13:00 +0200",
        checkout = "2024-06-02T11:00 +0200",
    )
    val milan = Place(name = "Milan", timeZone = CENTRAL_EUROPEAN_SUMMER)
    val milanHotel = Lodging(
        name = "UNAHOTELS Galles Milano",
        city = milan,
        address = "Piazza Lima, 2, 20124 Milano MI, Italy",
        checkIn = "2024-06-02T13:00 +0200",
        checkout = "2024-06-04T11:00 +0200",
    )
    val naples = Place(name = "Naples", timeZone = CENTRAL_EUROPEAN_SUMMER)
    val sorento = Place(name = "Sorento", timeZone = CENTRAL_EUROPEAN_SUMMER)
    val sorentoHotel = Lodging(
        name = "Hotel Conca Park",
        address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
        city = sorento,
        checkIn = "2024-06-12T13:00 +0200",
        checkout = "2024-06-14T11:00 +0200",
    )
    val nap = Airport("NAP", "Naples International Airport", city = naples)
    val flightToNewYork = Flight(
        FlightSegment(
            airportFrom = nap,
            airportTo = lis,
            departure = "2024-06-14T12:30 +0200",
            arrival = "2024-06-14T14:50 +0100",
        ),
        FlightSegment(
            airportFrom = lis,
            airportTo = jfk,
            departure = "2024-06-14T17:05 +0100",
            arrival = "2024-06-14T20:05 -0400",
        ),
    )
    val trip = Trip(
        id = "minhaTrip", flights = listOf(
            flightToLisbon,
            flightToParis,
            flightToNewYork,
        ), lodgings = listOf(
            lisbonAirBnB,
            portoHotel,
            parisAirBnB,
            niceHotel,
            milanHotel,
            sorentoHotel,
        )
    )

    val tripList = listOf(trip)
}

private fun Flight(vararg segments: FlightSegment) = Flight(segments = segments.asList())

private fun FlightSegment(
    airportFrom: Airport,
    departure: String,
    airportTo: Airport,
    arrival: String,
): FlightSegment {
    return FlightSegment(
        airportFrom,
        departure,
        airportTo,
        arrival,
    )
}

private fun Lodging(
    id: String,
    name: String? = null,
    address: String? = null,
    city: Place? = null,
    checkIn: String? = null,
    checkout: String? = null,
) = Lodging(
    id,
    name,
    address,
    city,
    checkIn,
    checkout,
)
