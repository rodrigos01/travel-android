package com.combah.travel2.model.network

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.LodgingSearchResult
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace
import java.util.TimeZone

fun ApiData.Airport.toAppDataModel() = Airport(
    iata = iata,
    name = name,
    timeZone = TimeZone.getTimeZone(timezone),
    city = city.toAppDataModel(),
)

fun ApiData.SimplePlace.toAppDataModel() = SimplePlace(
    name = name,
    address = address,
    city = city.toAppDataModel(),
)

fun ApiData.Place.toAppDataModel() = Place(
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
