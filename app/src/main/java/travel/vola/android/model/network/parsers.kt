package travel.vola.android.model.network

import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.data.LodgingSearchResult
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.PlaceDetailsResult
import travel.vola.android.model.data.SimplePlace
import java.util.TimeZone

fun ApiData.Airport.toAppDataModel() = Airport(
    iata = iata,
    name = name,
    timeZone = TimeZone.getTimeZone(timezone),
    city = city.toAppDataModel(),
)

fun ApiData.AirportSearchResult.toAppDataModel() = AirportSearchResult(
    iata = iata,
    name = name,
    location = location,
)

fun ApiData.SimplePlace.toAppDataModel() = SimplePlace(
    id = id,
    name = name,
    address = address.orEmpty(),
)

fun ApiData.Place.toAppDataModel(): Place = Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)

fun ApiData.LodgingSearchResult.toAppDataModel() = LodgingSearchResult(
    id = id,
    name = name,
    rating = rating,
    reviewCount = reviewCount,
    stars = stars,
    address = address,
    latitude = latitude,
    longitude = longitude,
    coverImage = coverImage,
    price = price,
    totalPrice = totalPrice,
)

fun ApiResponse.PlaceDetails.toAppDataModel() = PlaceDetailsResult(
    place = place.toAppDataModel(),
    city = city?.toAppDataModel(),
)
