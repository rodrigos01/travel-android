package travel.vola.android.model.room

import androidx.room.TypeConverter
import travel.vola.android.extensions.asISO8601String
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import java.time.ZonedDateTime
import java.util.TimeZone

fun RoomData.Trip.toAppDataModel(): Trip {
    val flights = flights.map { it.toAppDataModel() }
    val lodgings = lodgings.map { it.toAppDataModel() }
    val places = places.map { it.toAppDataModel() }
    val image =
        entity.coverImage ?: lodgings.firstOrNull()?.city?.coverImage
        ?: flights.firstOrNull()?.segments?.firstOrNull()?.airportTo?.city?.coverImage
    return Trip(
        id = entity.id,
        name = entity.name,
        coverImage = image,
        flights = flights,
        lodgings = lodgings,
        places = places,
    )
}

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

class Converters {
    @TypeConverter
    fun parseZonedDateTime(value: String): ZonedDateTime = ZonedDateTime.parse(value)

    @TypeConverter
    fun encodeZonedDateTime(value: ZonedDateTime): String = value.asISO8601String()

    @TypeConverter
    fun parseTimeZone(value: String): TimeZone = TimeZone.getTimeZone(value)

    @TypeConverter
    fun encodeTimeZone(value: TimeZone): String = value.id
}