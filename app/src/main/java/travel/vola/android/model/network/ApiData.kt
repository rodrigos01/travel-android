package travel.vola.android.model.network

import kotlinx.serialization.Serializable

interface ApiResponse {
    @Serializable
    data class AirportAutoComplete(
        val data: List<ApiData.Airport>,
    )

    @Serializable
    data class PlaceAutoComplete(
        val data: List<ApiData.SimplePlace>
    )

    @Serializable
    data class CityAutoComplete(
        val data: List<ApiData.Place>
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
    data class Place(
        val id: String,
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val coverImage: String?,
        val externalId: String,
        val source: String,
    )

    @Serializable
    data class SimplePlace(
        val name: String?,
        val address: String,
        val city: Place
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
}
