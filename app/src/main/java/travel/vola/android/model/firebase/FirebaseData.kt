package travel.vola.android.model.firebase

import com.google.firebase.firestore.Exclude
import java.util.UUID

sealed interface FirebaseData {
    data class Trip(
        @Exclude val id: String = "",
        val name: String? = null,
        val coverImage: String? = null,
        val flights: List<Flight> = emptyList(),
        val lodgings: List<Lodging> = emptyList(),
        val places: List<TimedPlace> = emptyList(),
        val restaurants: List<RestaurantReservation> = emptyList(),
    ) : FirebaseData

    data class Flight(
        val id: String = UUID.randomUUID().toString(),
        val segments: List<FlightSegment> = emptyList(),
        val price: Double? = null,
    ) : FirebaseData

    data class FlightSegment(
        val airportFrom: Airport? = null,
        val departure: String = "",
        val airportTo: Airport? = null,
        val arrival: String = "",
        val cityFrom: Place? = null,
        val cityTo: Place? = null,
    ) : FirebaseData

    data class Airport(
        val iata: String? = null,
        val name: String? = null,
        val timezone: String? = null,
        val city: Place? = null,
    )

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

    data class TimedPlace(
        val id: String = "",
        val place: Place = Place(),
        val time: String? = null,
        val hasTime: Boolean = false,
        val endTime: String? = null,
        val hasEndTime: Boolean = false,
        val city: Place? = null,
    )

    data class RestaurantReservation(
        val id: String = "",
        val time: String? = null,
        val place: Place = Place(),
        val city: Place? = null,
    )
}
