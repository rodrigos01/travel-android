package travel.vola.android.model.firebase

import com.google.firebase.firestore.Exclude
import kotlinx.serialization.Serializable
import java.util.UUID

// These classes are Firestore's document shape, and are also reused verbatim
// as the JSON payloads sent to the travel-node command API (@Serializable)
// since the API writes the exact same shape to Firestore server-side — see
// ApiTripCommandDataSource.
sealed interface FirebaseData {
    @Serializable
    data class Trip(
        @Exclude val id: String = "",
        val name: String? = null,
        val coverImage: String? = null,
        val preferences: TripPreferences? = null,
        val flights: List<Flight> = emptyList(),
        val lodgings: List<Lodging> = emptyList(),
        val places: List<TimedPlace> = emptyList(),
        val restaurants: List<RestaurantReservation> = emptyList(),
        val flexibleSections: List<FlexibleDaySection> = emptyList(),
    ) : FirebaseData

    @Serializable
    data class Flight(
        val id: String = UUID.randomUUID().toString(),
        val segments: List<FlightSegment> = emptyList(),
        val price: Double? = null,
    ) : FirebaseData

    @Serializable
    data class FlightSegment(
        val airportFrom: Airport? = null,
        val departure: String = "",
        val airportTo: Airport? = null,
        val arrival: String = "",
        val cityFrom: Place? = null,
        val cityTo: Place? = null,
    ) : FirebaseData

    @Serializable
    data class Airport(
        val iata: String? = null,
        val name: String? = null,
        val timezone: String? = null,
        val city: Place? = null,
    )

    @Serializable
    data class Lodging(
        val id: String = UUID.randomUUID().toString(),
        val name: String? = null,
        val address: String? = null,
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val city: Place? = null,
        val checkIn: String? = null,
        val checkout: String? = null,
    ) : FirebaseData

    @Serializable
    data class Place(
        val id: String = "",
        val name: String = "",
        val address: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val timeZone: String = "",
        val coverImage: String? = null,
        val externalId: String = "",
        val source: String = "",
    )

    @Serializable
    data class TimedPlace(
        val id: String = "",
        val place: Place = Place(),
        val time: String? = null,
        val hasTime: Boolean = false,
        val endTime: String? = null,
        val hasEndTime: Boolean = false,
        val city: Place? = null,
    )

    @Serializable
    data class RestaurantReservation(
        val id: String = "",
        val time: String? = null,
        val place: Place = Place(),
        val city: Place? = null,
    )

    @Serializable
    data class TripPreferences(
        val basicInformation: BasicInformation = BasicInformation(),
        val initialParameters: TripParameters = TripParameters(),
        val questionsAnswers: List<AnsweredQuestion> = emptyList(),
    )

    @Serializable
    data class BasicInformation(
        val groupType: GroupType = GroupType.SOLO,
        val travelers: Int = 0,
    )

    @Serializable
    enum class GroupType {
        SOLO,
        FAMILY,
        FRIENDS,
        COWORKERS,
        COUPLE,
    }

    @Serializable
    data class TripParameters(
        val occasions: List<String> = emptyList(),
        val interests: List<String> = emptyList(),
        val vibe: List<String> = emptyList(),
        val focus: List<String> = emptyList(),
        val mustHave: List<String> = emptyList(),
        val duration: List<String> = emptyList(),
        val anythingElse: String = "",
    )

    @Serializable
    data class AnsweredQuestion(
        val question: String = "",
        val answer: String = "",
    )

    @Serializable
    data class FlexibleDaySection(
        val id: String = "",
        val name: String = "",
        val date: String = "",
        val city: Place = Place(),
        val categories: List<FlexibleSectionCategory> = emptyList(),
    )

    @Serializable
    data class FlexibleSectionCategory(
        val name: String = "",
        val items: List<FlexibleSectionItem> = emptyList(),
    )

    @Serializable
    data class FlexibleSectionItem(
        val id: String = "",
        val place: Place = Place(),
        val note: String = "",
    )
}
