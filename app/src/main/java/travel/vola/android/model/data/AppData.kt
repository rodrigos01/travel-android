package travel.vola.android.model.data

import java.time.ZonedDateTime
import java.util.TimeZone

enum class ServerStatus {
    OK, UNAVAILABLE,
}

enum class DataSourceType {
    LOCAL, FIREBASE,
}

interface Identifiable {
    val id: String
}

data class Trip(
    val id: String,
    val name: String?,
    val coverImage: String?,
    val preferences: TripPreferences?,
    val flights: List<Flight>,
    val lodgings: List<Lodging>,
    val places: List<TimedPlace>,
    val restaurants: List<RestaurantReservation>,
    val flexibleSections: List<FlexibleDaySection>,
)

sealed interface TripEntity : Identifiable

sealed interface TripEvent

sealed interface WithCity {
    val city: Place
}

sealed interface Mapeable : WithCity

data class Flight(
    override val id: String,
    val segments: List<FlightSegment>,
    val price: Double? = null,
) : TripEntity

data class FlightSegment(
    val airportFrom: Airport,
    val departure: ZonedDateTime,
    val airportTo: Airport,
    val arrival: ZonedDateTime,
) : TripEvent

data class Airport(
    val iata: String,
    val name: String,
    val timeZone: TimeZone,
    val city: Place,
)

data class Lodging(
    override val id: String,
    val name: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    override val city: Place,
    val checkIn: ZonedDateTime,
    val checkout: ZonedDateTime,
) : TripEntity, TripEvent, Mapeable

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val coverImage: String?,
    val externalId: String,
    val timeZone: TimeZone,
    val source: String,
) {
    override fun equals(other: Any?): Boolean = other is Place && other.id == this.id
    override fun hashCode(): Int {
        return super.hashCode()
    }
}

data class PlaceDetailsResult(
    val place: Place,
    val city: Place?,
)

data class TimedPlace(
    override val id: String,
    val startDateTime: ZonedDateTime,
    val hasStartTime: Boolean,
    val endDateTime: ZonedDateTime?,
    val hasEndTime: Boolean,
    val place: Place,
    override val city: Place,
) : TripEntity, TripEvent, Mapeable

data class RestaurantReservation(
    override val id: String,
    val dateTime: ZonedDateTime,
    val place: Place,
    override val city: Place,
) : TripEntity, TripEvent, Mapeable

data class FlexibleDaySection(
    override val id: String,
    val name: String,
    val date: ZonedDateTime,
    val categories: List<FlexibleDayCategory>,
    override val city: Place,
) : TripEntity, TripEvent, Mapeable

data class FlexibleDayCategory(
    val name: String,
    val items: List<FlexibleDayItem>,
)

data class FlexibleDayItem(
    val id: String,
    val place: Place,
    val note: String,
)

data class AirportSearchResult(
    val iata: String,
    val name: String,
    val location: String,
)

data class SimplePlace(
    val id: String,
    val name: String,
    val address: String,
)

data class LodgingSearchResult(
    val id: String,
    val name: String,
    val coverImage: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val stars: Int,
    val price: Double,
    val totalPrice: Double,
    val latitude: Double,
    val longitude: Double,
)

data class TripPreferences(
    val basicInformation: BasicInformation,
    val initialParameters: TripParameters,
    val questionsAnswers: List<AnsweredQuestion>,
)

data class BasicInformation(
    val groupType: GroupType,
    val travelers: Int,
)

enum class GroupType {
    SOLO, FAMILY, FRIENDS, COWORKERS, COUPLE
}

data class TripParameters(
    val occasions: List<String>,
    val interests: List<String>,
    val vibe: List<String>,
    val focus: List<String>,
    val mustHave: List<String>,
    val duration: List<String>,
    val anythingElse: String,
)

data class AnsweredQuestion(
    val question: String,
    val answer: String,
)

data class SuggestionPlaceholder(val timestamp: ZonedDateTime, override val city: Place) :
        TripEvent, WithCity

