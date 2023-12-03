package com.combah.travel2.model.repository.mock

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val EAST_DAYLIGHT = "GMT-04:00"
private const val WESTERN_EUROPEAN_SUMMER = "GMT+01:00"
private const val CENTRAL_EUROPEAN_SUMMER = "GMT+02:00"

object MockData {
    val nyc = Place(name = "New York")
    val jfk = Airport("JFK", "JFK International Airport", city = nyc)
    val lisbon = Place(name = "Lisbon")
    val lis = Airport("LIS", "Humberto Delgado International Airport", city = lisbon)
    val flightToLisbon = Flight(
        FlightSegment(
            airportFrom = jfk,
            airportTo = lis,
            departure = timestampFromString("2024-05-10T22:05 $EAST_DAYLIGHT"),
            arrival = timestampFromString("2024-05-11T10:00 $WESTERN_EUROPEAN_SUMMER"),
        )
    )
    val lisbonAirBnB = Lodging(
        name = null,
        address = "R. Prof. Branco Rodrigues 2, 1200-422 Lisboa, Portugal",
        city = lisbon,
        checkIn = timestampFromString("2024-05-11T13:00 $WESTERN_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-05-19T11:00 $WESTERN_EUROPEAN_SUMMER"),
    )
    val porto = Place(name = "Porto")
    val opo = Airport("OPO", "Francisco Sá Carneiro Airport", city = porto)
    val portoHotel = Lodging(
        name = "Pestana Porto - A Brasileira",
        address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
        city = porto,
        checkIn = timestampFromString("2024-05-19T13:00 $WESTERN_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-05-21T11:00 $WESTERN_EUROPEAN_SUMMER"),
    )
    val paris = Place(name = "Paris")
    val ory = Airport("ORY", "Orly International Airport", city = paris)
    val flightToParis = Flight(
        FlightSegment(
            airportFrom = opo,
            airportTo = ory,
            departure = timestampFromString("2024-05-21T16:50 $WESTERN_EUROPEAN_SUMMER"),
            arrival = timestampFromString("2024-05-21T20:15 $CENTRAL_EUROPEAN_SUMMER"),
        )
    )
    val parisAirBnB = Lodging(
        name = null,
        address = "155 Rue du Faubourg Saint-Antoine, 75011 Paris, France",
        city = paris,
        checkIn = timestampFromString("2024-05-21T13:00 $CENTRAL_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-05-29T11:00 $CENTRAL_EUROPEAN_SUMMER"),
    )
    val nice = Place(name = "Nice")
    val niceHotel = Lodging(
        name = "Hôtel La Villa Nice Victor Hugo",
        address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
        city = nice,
        checkIn = timestampFromString("2024-05-29T13:00 $CENTRAL_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-06-02T11:00 $CENTRAL_EUROPEAN_SUMMER"),
    )
    val milan = Place(name = "Milan")
    val milanHotel = Lodging(
        name = "UNAHOTELS Galles Milano",
        city = milan,
        address = "Piazza Lima, 2, 20124 Milano MI, Italy",
        checkIn = timestampFromString("2024-06-02T13:00 $CENTRAL_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-06-04T11:00 $CENTRAL_EUROPEAN_SUMMER"),
    )
    val naples = Place(name = "Naples")
    val sorento = Place(name = "Sorento")
    val sorentoHotel = Lodging(
        name = "Hotel Conca Park",
        address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
        city = sorento,
        checkIn = timestampFromString("2024-06-12T13:00 $CENTRAL_EUROPEAN_SUMMER"),
        checkout = timestampFromString("2024-06-14T11:00 $CENTRAL_EUROPEAN_SUMMER"),
    )
    val nap = Airport("NAP", "Naples International Airport", city = naples)
    val flightToNewYork = Flight(
        FlightSegment(
            airportFrom = nap,
            airportTo = lis,
            departure = timestampFromString("2024-06-14T12:30 $CENTRAL_EUROPEAN_SUMMER"),
            arrival = timestampFromString("2024-06-14T14:50 $WESTERN_EUROPEAN_SUMMER"),
        ),
        FlightSegment(
            airportFrom = lis,
            airportTo = jfk,
            departure = timestampFromString("2024-06-14T17:05 $WESTERN_EUROPEAN_SUMMER"),
            arrival = timestampFromString("2024-06-14T20:05 $EAST_DAYLIGHT"),
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

fun timestampFromString(value: String, pattern: String? = "yyyy-MM-dd'T'HH:mm z"): Time {
    val timeInMillis = SimpleDateFormat(pattern, Locale.getDefault()).parse(value)?.time
        ?: error("Null return from parsing $value with $pattern")
    val timeZoneId = value.split(" ").last()
    val timeZone = TimeZone.getTimeZone(timeZoneId)
    if (timeZone.id != timeZoneId) {
        error("Invalid Timezone in $value")
    }
    return Time(timeInMillis, TimeZone.getTimeZone(timeZoneId))
}