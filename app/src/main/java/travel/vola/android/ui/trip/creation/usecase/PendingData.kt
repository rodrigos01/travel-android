package travel.vola.android.ui.trip.creation.usecase

import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import java.time.ZonedDateTime

sealed interface PendingData {
    val id: String

    data class PendingFlight(
        override val id: String,
        val entityId: String? = null,
        val departure: ZonedDateTime,
        val departureTimeSet: Boolean = false,
        val airportFrom: Airport? = null,
        val airportTo: Airport? = null,
        val arrival: ZonedDateTime? = null,
        val arrivalTimeSet: Boolean = false,
        val airportFromSearchResults: List<AirportSearchResult> = emptyList(),
        val airportToSearchResults: List<AirportSearchResult> = emptyList(),
    ) : PendingData

    data class PendingLodging(
        override val id: String,
        val entityId: String? = null,
        val checkIn: ZonedDateTime,
        val isCheckInTimeSet: Boolean = false,
        val checkOut: ZonedDateTime? = null,
        val isCheckOutTimeSet: Boolean = false,
        val name: String? = null,
        val address: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val city: Place? = null,
        val searchResults: List<SimplePlace> = emptyList(),
    ) : PendingData

    data class LodgingSearchParams(
        override val id: String,
        val checkIn: ZonedDateTime,
        val checkOut: ZonedDateTime? = null,
        val city: Place? = null,
        val searchResults: List<Place> = emptyList(),
    ) : PendingData

    data class PendingTimedPlace(
        override val id: String,
        val entityId: String? = null,
        val startDateTime: ZonedDateTime,
        val hasStartTime: Boolean = false,
        val endDateTime: ZonedDateTime? = null,
        val hasEndTime: Boolean = false,
        val place: Place? = null,
        val city: Place? = null,
        val searchResults: List<SimplePlace> = emptyList(),
    ) : PendingData

    data class PendingRestaurant(
        override val id: String,
        val entityId: String? = null,
        val dateTime: ZonedDateTime,
        val hasTime: Boolean = false,
        val place: Place? = null,
        val city: Place? = null,
        val searchResults: List<SimplePlace> = emptyList(),
    ) : PendingData

    data class PendingFlexibleSection(
        override val id: String,
        val startDateTime: ZonedDateTime,
        val hasStartTime: Boolean = false,
        val sectionName: String,
        val city: Place,
        val categories: List<FlexibleDayCategory>,
    ) : PendingData
}
