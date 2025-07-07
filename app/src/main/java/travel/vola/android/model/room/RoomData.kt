package travel.vola.android.model.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.ZonedDateTime
import java.util.TimeZone

sealed interface RoomData {

    data class Trip(
        @Embedded val entity: Schema.Trip,
        @Relation(
            entity = Schema.Flight::class, parentColumn = "id", entityColumn = "tripId"
        ) val flights: List<Flight>,
        @Relation(
            entity = Schema.Lodging::class, parentColumn = "id", entityColumn = "tripId"
        ) val lodgings: List<Lodging>,
        @Relation(
            entity = Schema.TimedPlace::class, parentColumn = "id", entityColumn = "tripId"
        ) val places: List<TimedPlace>,
    )

    data class Flight(
        @Embedded val entity: Schema.Flight,
        @Relation(
            entity = Schema.FlightSegment::class, parentColumn = "id", entityColumn = "flightId"
        ) val segments: List<FlightSegment>,
    )

    data class FlightSegment(
        @Embedded val entity: Schema.FlightSegment,
        @Relation(
            entity = Schema.Airport::class, parentColumn = "airportFrom", entityColumn = "iata"
        ) val airportFrom: Airport,
        @Relation(
            entity = Schema.Airport::class, parentColumn = "airportTo", entityColumn = "iata"
        ) val airportTo: Airport,
    )

    data class Airport(
        @Embedded val entity: Schema.Airport,
        @Relation(
            parentColumn = "city", entityColumn = "id"
        ) val city: Place,
    )

    data class Lodging(
        @Embedded val entity: Schema.Lodging,
        @Relation(
            parentColumn = "city", entityColumn = "id"
        ) val city: Place,
    )

    data class TimedPlace(
        @Embedded val entity: Schema.TimedPlace,
        @Relation(
            parentColumn = "place", entityColumn = "id"
        ) val place: Place,
        @Relation(
            parentColumn = "city", entityColumn = "id"
        ) val city: Place,
    )

    @Entity
    data class Place(
        @PrimaryKey val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val coverImage: String?,
        val externalId: String,
        val source: String,
    )

    interface Schema {

        @Entity
        data class Trip(
            @PrimaryKey val id: String,
            val name: String?,
            val coverImage: String?,
        )

        @Entity
        data class Flight(
            @PrimaryKey val id: String,
            val tripId: String,
            val price: Double? = null,
        )

        @Entity
        data class FlightSegment(
            @PrimaryKey val id: String,
            val flightId: String,
            val airportFrom: String,
            val departure: ZonedDateTime,
            val airportTo: String,
            val arrival: ZonedDateTime,
        )

        @Entity
        data class Airport(
            @PrimaryKey val iata: String,
            val name: String,
            val timeZone: TimeZone,
            val city: String,
        )

        @Entity
        data class Lodging(
            @PrimaryKey val id: String,
            val tripId: String,
            val name: String?,
            val address: String,
            val latitude: Double,
            val longitude: Double,
            val city: String,
            val checkIn: ZonedDateTime,
            val checkout: ZonedDateTime,
        )

        @Entity
        data class TimedPlace(
            @PrimaryKey val id: String,
            val tripId: String,
            val startDateTime: ZonedDateTime,
            val hasStartTime: Boolean,
            val endDateTime: ZonedDateTime?,
            val hasEndTime: Boolean,
            val place: String,
            val city: String,
        )
    }
}