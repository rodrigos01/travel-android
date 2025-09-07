package travel.vola.android.ui.lodgingsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.MutableMapStateFlow
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.get
import travel.vola.android.extensions.remove
import travel.vola.android.extensions.set
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.LodgingSearchResult
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Time
import travel.vola.android.model.network.toAppDataModel
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.lodgingsearch.composable.LodgingSearchDestination
import travel.vola.android.ui.lodgingsearch.state.LodgingDetailsState
import travel.vola.android.ui.lodgingsearch.state.LodgingReviewState
import travel.vola.android.ui.lodgingsearch.state.LodgingRoomOfferState
import travel.vola.android.ui.lodgingsearch.state.LodgingSearchResultState
import java.util.TimeZone

class LodgingSearchViewModel(
    private val tripId: String,
    private val repository: LodgingSearchRepository,
    private val tripRepository: TripRepository,
    placeRepository: PlaceRepository,
    locationId: String,
    checkInMillis: Long,
    checkOutMillis: Long,
    timeZoneId: String,
) : ViewModel() {
    interface UiState {
        val searchState: SearchParamsState
        val localState: LocalState
        val sortAndFilterState: SortAndFilterState

        data class Loading(
            override val searchState: SearchParamsState,
            override val localState: LocalState = LocalState(),
            override val sortAndFilterState: SortAndFilterState = SortAndFilterState(),
        ) : UiState

        data class Loaded(
            override val searchState: SearchParamsState,
            val results: List<LodgingSearchResultState>,
            val openedResults: Map<String, LodgingDetailsState> = emptyMap(),
            val selectedResult: LodgingDetailsState? = null,
            override val localState: LocalState = LocalState(),
            override val sortAndFilterState: SortAndFilterState,
        ) : UiState

        data class Error(
            override val searchState: SearchParamsState,
            override val localState: LocalState = LocalState(),
            override val sortAndFilterState: SortAndFilterState = SortAndFilterState(),
        ) : UiState
    }

    data class LocalState(
        val showAddConfirmation: Boolean = false,
        val selectedResultId: String? = null,
    )

    data class SearchParamsState(
        val checkIn: Time,
        val checkOut: Time,
        val locationText: String,
        val minCheckOut: Time? = null,
    )

    data class SortAndFilterState(
        val sortOption: SortOption = SortOption.BEST,
        val minRating: Double = 0.0,
        val minStars: Int = 0,
        val availablePriceRange: ClosedFloatingPointRange<Double> = 0.0..10000.0,
        val priceRange: ClosedFloatingPointRange<Double> = availablePriceRange,
    )

    enum class SortOption {
        BEST, RATING, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW
    }

    enum class StarOption(val stars: Int) {
        ANY(0), THREE(3), FOUR(4), FIVE(5)
    }

    private val location =
        placeRepository.places[locationId] ?: error("Place with id $locationId not found")

    private val lodgingCities = mutableMapOf<String, Place>()

    private val loadingState = MutableStateFlow(false)
    private val searchParamsState = MutableStateFlow(
        SearchParamsState(
            checkIn = Time(checkInMillis, TimeZone.getTimeZone(timeZoneId)),
            checkOut = Time(checkOutMillis, TimeZone.getTimeZone(timeZoneId)),
            locationText = location.name,
        )
    )
    private val loadAttemptCountState = MutableStateFlow(0)
    private val searchResultState = combine(
        searchParamsState, loadAttemptCountState
    ) { params, _ -> params }.onEach { loadingState.value = true }.map {
        it to repository.search(
            locationId = location.id,
            checkIn = it.checkIn,
            checkOut = it.checkOut,
        )
    }.onEach {
        loadingState.value = false
    }
    private val sortAndFilterState = MutableStateFlow(SortAndFilterState())
    private val localState = MutableStateFlow(LocalState())
    private val openedResultsState = MutableMapStateFlow<String, LodgingDetailsState>()
    val uiState = combine(
        searchResultState,
        openedResultsState,
        loadingState,
        sortAndFilterState,
        localState,
    ) { (params, results), openedResults, loading, sortAndFilter, localState ->
        if (loading) {
            UiState.Loading(searchState = params, localState = localState)
        } else if (results.isEmpty()) {
            UiState.Error(
                searchState = params,
                localState = localState,
                sortAndFilterState = sortAndFilter,
            )
        } else {
            UiState.Loaded(
                searchState = params,
                localState = localState,
                sortAndFilterState = if (results.isNotEmpty()) {
                    sortAndFilter.copy(availablePriceRange = results.minOf { it.price }..results.maxOf { it.price })
                } else sortAndFilter,
                results = results.asSequence().sortedBy { it.sortValue(sortAndFilter.sortOption) }
                    .filter { it.passesFilter(sortAndFilter) }.map {
                        LodgingSearchResultState(
                            id = it.id,
                            name = it.name,
                            coverImage = it.coverImage,
                            address = it.address,
                            rating = it.rating,
                            reviewCount = it.reviewCount,
                            lodgingType = "${it.stars}-star hotel",
                            price = it.price,
                            latitude = it.latitude,
                            longitude = it.longitude,
                        )
                    }.toList(),
                openedResults = openedResults,
                selectedResult = openedResults[localState.selectedResultId],
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, UiState.Loading(
            searchState = searchParamsState.value, localState = localState.value
        )
    )

    fun onSortOptionSelected(option: SortOption) {
        sortAndFilterState.value = sortAndFilterState.value.copy(sortOption = option)
    }

    private fun LodgingSearchResult.sortValue(sortOption: SortOption): Double {
        return when (sortOption) {
            SortOption.BEST -> 0.0
            SortOption.RATING -> -rating
            SortOption.PRICE_LOW_TO_HIGH -> price
            SortOption.PRICE_HIGH_TO_LOW -> -price
        }
    }

    fun onFiltersApplied(
        minRating: Double, minStars: Int, priceRange: ClosedFloatingPointRange<Double>,
    ) {
        sortAndFilterState.value = sortAndFilterState.value.copy(
            minRating = minRating,
            minStars = minStars,
            priceRange = priceRange,
        )
    }

    fun onRetryTapped() {
        loadAttemptCountState.value++
    }

    private fun LodgingSearchResult.passesFilter(filters: SortAndFilterState): Boolean {
        return rating >= filters.minRating && stars >= filters.minStars && price in filters.priceRange
    }

    fun onLodgingTapped(lodgingId: String?) {
        localState.value = localState.value.copy(selectedResultId = lodgingId)
        if (lodgingId == null || openedResultsState[lodgingId] != null) {
            return
        }
        val state = uiState.value as? UiState.Loaded ?: return
        val existingState = state.results.firstOrNull { it.id == lodgingId } ?: return
        val initialState = LodgingDetailsState(
            id = existingState.id,
            name = existingState.name,
            rating = existingState.rating,
            reviewCount = existingState.reviewCount,
            lodgingType = existingState.lodgingType,
            photos = listOf(existingState.coverImage),
            checkIn = state.searchState.checkIn,
            checkOut = state.searchState.checkOut,
            price = existingState.price,
            rooms = emptyList(),
            description = null,
            address = existingState.address,
            latitude = existingState.latitude,
            longitude = existingState.longitude,
            isLoading = true,
        )
        openedResultsState[lodgingId] = initialState
        viewModelScope.launch {
            val lodging = repository.details(
                lodgingId,
                searchParamsState.value.checkIn,
                searchParamsState.value.checkOut,
                latitude = initialState.latitude,
                initialState.longitude
            ) ?: return@launch
            openedResultsState[lodgingId] = initialState.copy(
                photos = initialState.photos + lodging.photos.subList(1, lodging.photos.size),
                reviewCount = lodging.reviewCount,
                latitude = lodging.latitude,
                longitude = lodging.longitude,
                rooms = lodging.rooms.map { offer ->
                    LodgingRoomOfferState(
                        photos = offer.photos,
                        description = offer.name,
                        breakfastIncluded = offer.features.breakfast,
                        refundable = offer.features.refundable,
                        prePaymentRequired = offer.features.prePayment,
                        isAllInclusive = offer.features.allInclusive,
                        price = offer.price,
                        bookingUrl = offer.bookingUrl,
                        bookingAgency = offer.bookingAgency,
                    )
                },
                description = lodging.description,
                reviewsSource = lodging.reviewsSource,
                reviewsUrl = lodging.reviewsUrl,
                reviews = lodging.reviews.map {
                    LodgingReviewState(
                        rating = it.rating,
                        ratingImageUrl = it.ratingImageUrl,
                        reviewTime = Time(it.reviewTime),
                        tripDate = Time(it.travelDate),
                        authorAvatarUrl = it.avatarUrl,
                        authorName = it.userName,
                        authorLocation = it.userLocation,
                        title = it.title,
                        review = it.text,
                    )
                },
                isLoading = false,
            )
            lodging.city?.toAppDataModel()?.let { lodgingCities[lodgingId] = it }
        }
    }

    fun onLodgingClosed(lodgingId: String) {
        lodgingCities.remove(lodgingId)
        openedResultsState.remove(lodgingId)
    }

    fun onAddLodgingTapped(lodgingId: String) {
        val details = openedResultsState[lodgingId] ?: return
        val lodging = Lodging(
            id = lodgingId,
            name = details.name,
            address = details.address,
            latitude = details.latitude,
            longitude = details.longitude,
            city = lodgingCities[lodgingId] ?: location,
            checkIn = details.checkIn,
            checkout = details.checkOut,
        )
        viewModelScope.launch {
            tripRepository.saveLodging(tripId, lodging)
            localState.value = localState.value.copy(showAddConfirmation = true)
        }
    }

    fun onContinueBrowsingTapped() {
        localState.value = localState.value.copy(showAddConfirmation = false)
    }

    class Factory(params: LodgingSearchDestination.Params) :
        ViewModelProvider.Factory by viewModelFactory(initializer = {
            LodgingSearchViewModel(
                params.tripId,
                LodgingSearchRepository(),
                factoryDependencies.tripRepository,
                factoryDependencies.placeRepository,
                params.locationId,
                params.checkIn,
                params.checkOut,
                params.timeZoneId,
            )
        })
}