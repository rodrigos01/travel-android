package travel.vola.android.model.data

import kotlinx.serialization.Serializable
import java.util.TimeZone

enum class ServerStatus {
    OK,
    UNAVAILABLE,
}

interface Identifiable {
    val id: String
}

data class Trip(
    val id: String,
    val name: String?,
    val coverImage: String?,
    val flights: List<Flight>,
    val lodgings: List<Lodging>,
    val places: List<Place>,
)

sealed interface TripEntity : Identifiable

sealed interface TripEvent

data class Flight(
    override val id: String,
    val segments: List<FlightSegment>,
    val price: Double? = null,
) : TripEntity

data class FlightSegment(
    val airportFrom: Airport,
    val departure: Time,
    val airportTo: Airport,
    val arrival: Time,
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
    val city: Place,
    val checkIn: Time,
    val checkout: Time,
) : TripEntity, TripEvent

@Serializable
data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val coverImage: String?,
    val externalId: String,
    val source: String,
) {
    override fun equals(other: Any?): Boolean = other is Place && other.id == this.id
    override fun hashCode(): Int {
        return super.hashCode()
    }
}

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
