package travel.vola.android.model.network

import kotlinx.serialization.Serializable

interface ApiResponse {
    @Serializable
    data class AirportAutoComplete(
        val data: List<ApiData.AirportSearchResult>,
    )

    @Serializable
    data class PlaceAutoComplete(
        val results: List<ApiData.SimplePlace>,
    )

    @Serializable
    data class CityAutoComplete(
        val data: List<ApiData.Place>,
    )

    @Serializable
    data class PlaceDetails(
        val place: ApiData.Place,
        val city: ApiData.Place?,
    )

    @Serializable
    data class LodgingSearch(
        val hotels: List<ApiData.LodgingSearchResult>,
    )
}

interface ApiData {

    @Serializable
    data class Airport(
        val iata: String,
        val name: String,
        val timezone: String,
        val city: Place,
    )

    @Serializable
    data class AirportSearchResult(
        val iata: String,
        val name: String,
        val location: String,
    )

    @Serializable
    data class Place(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val coverImage: String?,
        val city: Place?,
        val externalId: String,
        val source: String,
    )

    @Serializable
    data class SimplePlace(
        val id: String,
        val name: String,
        val address: String,
    )

    @Serializable
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

    @Serializable
    data class LodgingDetails(
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
        val photos: List<String>,
        val rooms: List<LodgingOffer>,
        val description: String?,
        val reviewsUrl: String,
        val reviewsSource: String,
        val reviews: List<LodgingReview>,
        val city: Place?,
    )

    @Serializable
    data class LodgingOffer(
        val photos: List<String>,
        val name: String,
        val features: LodgingOfferFeatures,
        val price: Double,
        val totalPrice: Double,
        val bookingUrl: String,
        val bookingAgency: String,
    )

    @Serializable
    data class LodgingOfferFeatures(
        val breakfast: Boolean,
        val refundable: Boolean,
        val prePayment: Boolean,
        val allInclusive: Boolean,
    )

    @Serializable
    data class LodgingReview(
        val rating: Double,
        val ratingImageUrl: String,
        val reviewTime: String,
        val travelDate: String,
        val avatarUrl: String?,
        val userName: String,
        val userLocation: String?,
        val title: String,
        val text: String,
    )
}
