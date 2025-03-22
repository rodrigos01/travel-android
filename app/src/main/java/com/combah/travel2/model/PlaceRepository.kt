package com.combah.travel2.model

import com.combah.travel2.model.data.Place

class PlaceRepository {
    val places: MutableMap<String, Place> = mutableMapOf()
}