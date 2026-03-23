package travel.vola.android.model.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.TimeZone

sealed interface RoomData {

    data class Trip(
        @Embedded val entity: Schema.Trip,
        @Relation(
            entity = Schema.Flight::class,
            parentColumn = "id",
            entityColumn = "tripId",
        ) val flights: List<Flight>,
        @Relation(
            entity = Schema.Lodging::class,
            parentColumn = "id",
            entityColumn = "tripId",
        ) val lodgings: List<Lodging>,
        @Relation(
            entity = Schema.TimedPlace::class,
            parentColumn = "id",
            entityColumn = "tripId",
        ) val places: List<TimedPlace>,
        @Relation(
            entity = Schema.RestaurantReservation::class,
            parentColumn = "id",
            entityColumn = "tripId",
        ) val restaurants: List<RestaurantReservation>,
        @Relation(
            entity = Schema.FlexibleSection::class,
            parentColumn = "id",
            entityColumn = "tripId",
        ) val flexibleSections: List<FlexibleSection>,
    )

    data class Flight(
        @Embedded val entity: Schema.Flight,
        @Relation(
            entity = Schema.FlightSegment::class,
            parentColumn = "id",
            entityColumn = "flightId",
        ) val segments: List<FlightSegment>,
    )

    data class FlightSegment(
        @Embedded val entity: Schema.FlightSegment,
        @Relation(
            entity = Schema.Airport::class,
            parentColumn = "airportFrom",
            entityColumn = "iata",
        ) val airportFrom: Airport,
        @Relation(
            entity = Schema.Airport::class,
            parentColumn = "airportTo",
            entityColumn = "iata",
        ) val airportTo: Airport,
    )

    data class Airport(
        @Embedded val entity: Schema.Airport,
        @Relation(
            parentColumn = "city",
            entityColumn = "id",
        ) val city: Place,
    )

    data class Lodging(
        @Embedded val entity: Schema.Lodging,
        @Relation(
            parentColumn = "city",
            entityColumn = "id",
        ) val city: Place,
    )

    data class TimedPlace(
        @Embedded val entity: Schema.TimedPlace,
        @Relation(
            parentColumn = "place",
            entityColumn = "id",
        ) val place: Place,
        @Relation(
            parentColumn = "city",
            entityColumn = "id",
        ) val city: Place,
    )

    data class RestaurantReservation(
        @Embedded val entity: Schema.RestaurantReservation,
        @Relation(
            parentColumn = "place",
            entityColumn = "id",
        ) val place: Place,
        @Relation(
            parentColumn = "city",
            entityColumn = "id",
        ) val city: Place,
    )

    data class FlexibleSection(
        @Embedded val entity: Schema.FlexibleSection,
        @Relation(
            parentColumn = "city",
            entityColumn = "id",
        ) val city: Place,
        @Relation(
            entity = Schema.FlexibleSectionCategory::class,
            parentColumn = "id",
            entityColumn = "sectionId",
        ) val categories: List<FlexibleSectionCategory>,
    )

    data class FlexibleSectionCategory(
        @Embedded val entity: Schema.FlexibleSectionCategory,
        @Relation(
            entity = Schema.FlexibleSectionItem::class,
            parentColumn = "id",
            entityColumn = "categoryId",
        ) val items: List<FlexibleSectionItem>,
    )

    data class FlexibleSectionItem(
        @Embedded val entity: Schema.FlexibleSectionItem,
        @Relation(
            parentColumn = "place",
            entityColumn = "id",
        ) val place: Place,
    )

    @Entity
    data class Place(
        @PrimaryKey val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val timeZone: TimeZone?,
        val coverImage: String?,
        val externalId: String,
        val source: String,
    )

    @Serializable
    data class TripPreferences(
        val basicInformation: BasicInformation,
        val initialParameters: TripParameters,
        val questionsAnswers: List<AnsweredQuestion>,
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
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
        val anythingElse: String,
    )

    @Serializable
    data class AnsweredQuestion(
        val question: String,
        val answer: String,
    )

    interface Schema {

        @Entity
        data class Trip(
            @PrimaryKey val id: String,
            val name: String?,
            val coverImage: String?,
            val preferences: TripPreferences?,
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

        @Entity
        data class RestaurantReservation(
            @PrimaryKey val id: String,
            val tripId: String,
            val dateTime: ZonedDateTime,
            val place: String,
            val city: String,
        )

        @Entity
        data class FlexibleSection(
            @PrimaryKey val id: String,
            val tripId: String,
            val name: String,
            val date: ZonedDateTime,
            val city: String,
        )

        @Entity
        data class FlexibleSectionCategory(
            @PrimaryKey val id: String,
            val sectionId: String,
            val name: String,
        )

        @Entity
        data class FlexibleSectionItem(
            @PrimaryKey val id: String,
            val categoryId: String,
            val place: String,
            val note: String,
        )
    }
}
