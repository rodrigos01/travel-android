package com.combah.travel2.model.network

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place

fun ApiData.Airport.toAppDataModel() = Airport(
    iata = iata,
    name = name,
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