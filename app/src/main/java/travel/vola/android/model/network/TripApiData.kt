package travel.vola.android.model.network

import kotlinx.serialization.Serializable

// Wire format for the travel-node trips command API. Kept independent of
// FirebaseData.kt on purpose: the two shapes match today only because the
// API happens to persist what it's given straight to Firestore. Firestore's
// document shape is free to change on its own — this is the actual contract
// the client is bound to, and nothing enforces the two staying identical.
interface TripApiData {

    // Only the fields the caller sets are included on the wire; the API
    // treats a missing field as "leave this alone" (see TripRepositoryImpl's
    // putCommand callers).
    @Serializable
    data class TripUpdate(
        val name: String? = null,
        val preferences: TripPreferences? = null,
        val places: List<TimedPlace>? = null,
    )

    @Serializable
    data class Flight(
        val id: String,
        val segments: List<FlightSegment>,
        val price: Double? = null,
    )

    @Serializable
    data class FlightSegment(
        val airportFrom: Airport,
        val departure: String,
        val airportTo: Airport,
        val arrival: String,
        val cityFrom: Place? = null,
        val cityTo: Place? = null,
    )

    @Serializable
    data class Airport(
        val iata: String,
        val name: String,
        val timezone: String,
        val city: Place? = null,
    )

    @Serializable
    data class Lodging(
        val id: String,
        val name: String? = null,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val city: Place,
        val checkIn: String,
        val checkout: String,
    )

    @Serializable
    data class Place(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val timeZone: String,
        val coverImage: String? = null,
        val externalId: String,
        val source: String,
    )

    @Serializable
    data class TimedPlace(
        val id: String,
        val place: Place,
        val time: String? = null,
        val hasTime: Boolean,
        val endTime: String? = null,
        val hasEndTime: Boolean,
        val city: Place? = null,
    )

    @Serializable
    data class RestaurantReservation(
        val id: String,
        val time: String? = null,
        val place: Place,
        val city: Place? = null,
    )

    @Serializable
    data class TripPreferences(
        val basicInformation: BasicInformation,
        val initialParameters: TripParameters,
        val questionsAnswers: List<AnsweredQuestion> = emptyList(),
    )

    @Serializable
    data class BasicInformation(
        val groupType: GroupType,
        val travelers: Int,
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
        val question: String,
        val answer: String,
    )

    @Serializable
    data class FlexibleDaySection(
        val id: String,
        val name: String,
        val date: String,
        val city: Place,
        val categories: List<FlexibleSectionCategory>,
    )

    @Serializable
    data class FlexibleSectionCategory(
        val name: String,
        val items: List<FlexibleSectionItem>,
    )

    @Serializable
    data class FlexibleSectionItem(
        val id: String,
        val place: Place,
        val note: String = "",
    )
}
