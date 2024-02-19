package com.combah.travel2.model.network

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace

fun ApiData.Airport.toAppDataModel() = Airport(
    iata = iata,
    name = name,
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