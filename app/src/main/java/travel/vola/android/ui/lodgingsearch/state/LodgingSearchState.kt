package travel.vola.android.ui.lodgingsearch.state

import travel.vola.android.model.data.Time

data class LodgingSearchResultState(
    val id: String,
    val name: String,
    val coverImage: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val lodgingType: String,
    val price: Double,
    val latitude: Double,
    val longitude: Double,
)

data class LodgingDetailsState(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: Int,
    val lodgingType: String,
    val photos: List<String>,
    val checkIn: Time,
    val checkOut: Time,
    val price: Double,
    val rooms: List<LodgingRoomOfferState>,
    val description: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val reviewsUrl: String? = null,
    val reviewsSource: String? = null,
    val reviews: List<LodgingReviewState> = emptyList(),
    val isLoading: Boolean,
)

data class LodgingRoomOfferState(
    val photos: List<String>,
    val description: String,
    val breakfastIncluded: Boolean,
    val refundable: Boolean,
    val prePaymentRequired: Boolean,
    val isAllInclusive: Boolean,
    val price: Double,
    val bookingUrl: String,
    val bookingAgency: String,
)

data class LodgingReviewState(
    val reviewTime: Time,
    val tripDate: Time,
    val rating: Double,
    val ratingImageUrl: String,
    val authorAvatarUrl: String?,
    val authorName: String,
    val authorLocation: String?,
    val title: String,
    val review: String,
)