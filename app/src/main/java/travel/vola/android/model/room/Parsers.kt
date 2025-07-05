package travel.vola.android.model.room

import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip

fun RoomData.Trip.toAppDataModel(): Trip = Trip(
    id = entity.id,
    name = entity.name,
    coverImage = entity.coverImage,
    flights = flights.map { it.toAppDataModel() },
    lodgings = lodgings.map { it.toAppDataModel() },
    places = places.map { it.toAppDataModel() },
)

fun RoomData.Flight.toAppDataModel(): Flight = Flight(
    id = entity.id,
    segments = segments.map { it.toAppDataModel() },
    price = entity.price,
)

fun RoomData.FlightSegment.toAppDataModel(): FlightSegment = FlightSegment(
    airportFrom = airportFrom.toAppDataModel(),
    departure = entity.departure,
    airportTo = airportTo.toAppDataModel(),
    arrival = entity.arrival,
)

fun RoomData.Airport.toAppDataModel(): Airport = Airport(
    iata = entity.iata,
    name = entity.name,
    timeZone = entity.timeZone,
    city = city.toAppDataModel(),
)

fun RoomData.Lodging.toAppDataModel(): Lodging = Lodging(
    id = entity.id,
    name = entity.name,
    address = entity.address,
    latitude = entity.latitude,
    longitude = entity.longitude,
    city = city.toAppDataModel(),
    checkIn = entity.checkIn,
    checkout = entity.checkout,
)

fun RoomData.TimedPlace.toAppDataModel(): TimedPlace = TimedPlace(
    id = entity.id,
    startDateTime = entity.startDateTime,
    hasStartTime = entity.hasStartTime,
    endDateTime = entity.endDateTime,
    hasEndTime = entity.hasEndTime,
    place = place.toAppDataModel(),
    city = city.toAppDataModel(),
)

fun RoomData.Place.toAppDataModel(): Place = Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)