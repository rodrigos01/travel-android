package travel.vola.android.model.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import travel.vola.android.extensions.asISO8601String
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AnsweredQuestion
import travel.vola.android.model.data.BasicInformation
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripParameters
import travel.vola.android.model.data.TripPreferences
import java.time.ZonedDateTime
import java.util.TimeZone

fun RoomData.Trip.toAppDataModel(): Trip {
    val appFlights = flights.map { it.toAppDataModel() }
    val appLodgings = lodgings.map { it.toAppDataModel() }
    val appPlaces = places.map { it.toAppDataModel() }
    val appRestaurants = restaurants.map { it.toAppDataModel() }
    val image =
        entity.coverImage ?: appLodgings.firstOrNull()?.city?.coverImage
        ?: appFlights.firstOrNull()?.segments?.firstOrNull()?.airportTo?.city?.coverImage
        ?: appPlaces.firstOrNull()?.city?.coverImage
        ?: appRestaurants.firstOrNull()?.city?.coverImage
    return Trip(
        id = entity.id,
        name = entity.name,
        coverImage = image,
        preferences = TripPreferences(
            basicInformation = BasicInformation(
                groupType = entity.preferences?.basicInformation?.groupType?.toAppDataModel()
                    ?: GroupType.SOLO,
                travelers = entity.preferences?.basicInformation?.travelers
                    ?: 0,
            ),
            initialParameters = entity.preferences?.initialParameters.let {
                TripParameters(
                    occasions = it?.occasions ?: emptyList(),
                    interests = it?.interests ?: emptyList(),
                    vibe = it?.vibe ?: emptyList(),
                    focus = it?.focus ?: emptyList(),
                    mustHave = it?.mustHave ?: emptyList(),
                    duration = it?.duration ?: emptyList(),
                    anythingElse = it?.anythingElse ?: "",
                )
            },
            questionsAnswers = entity.preferences?.questionsAnswers?.map {
                AnsweredQuestion(
                    it.question,
                    it.answer
                )
            } ?: emptyList()
        ),
        flights = appFlights,
        lodgings = appLodgings,
        places = appPlaces,
        restaurants = appRestaurants,
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

fun RoomData.RestaurantReservation.toAppDataModel(): RestaurantReservation = RestaurantReservation(
    id = entity.id,
    dateTime = entity.dateTime,
    place = place.toAppDataModel(),
    city = city.toAppDataModel(),
)

fun RoomData.Place.toAppDataModel(): Place = Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    timeZone = timeZone ?: TimeZone.getDefault(),
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)

fun GroupType.toRoomDataModel(): RoomData.GroupType = when (this) {
    GroupType.SOLO -> RoomData.GroupType.SOLO
    GroupType.FAMILY -> RoomData.GroupType.FAMILY
    GroupType.FRIENDS -> RoomData.GroupType.FRIENDS
    GroupType.COWORKERS -> RoomData.GroupType.COWORKERS
    GroupType.COUPLE -> RoomData.GroupType.COUPLE
}

fun RoomData.GroupType.toAppDataModel(): GroupType = when (this) {
    RoomData.GroupType.SOLO -> GroupType.SOLO
    RoomData.GroupType.FAMILY -> GroupType.FAMILY
    RoomData.GroupType.FRIENDS -> GroupType.FRIENDS
    RoomData.GroupType.COWORKERS -> GroupType.COWORKERS
    RoomData.GroupType.COUPLE -> GroupType.COUPLE
}

class Converters {
    @TypeConverter
    fun parseZonedDateTime(value: String): ZonedDateTime = ZonedDateTime.parse(value)

    @TypeConverter
    fun encodeZonedDateTime(value: ZonedDateTime): String = value.asISO8601String()

    @TypeConverter
    fun parseTimeZone(value: String): TimeZone = TimeZone.getTimeZone(value)

    @TypeConverter
    fun encodeTimeZone(value: TimeZone): String = value.id

    @TypeConverter
    fun parseTripPreferences(value: String): RoomData.TripPreferences = Json.decodeFromString(value)

    @TypeConverter
    fun encodeTripPreferences(value: RoomData.TripPreferences): String = Json.encodeToString(value)
}