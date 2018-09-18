package com.combah.travel2.ui.data

import com.combah.travel2.model.data.Place
import java.util.*

open class TripEvent(
    val name: String,
    val location: String,
    val timestamp: Date,
    val place: Place
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripEvent) return false

        if (name != other.name) return false
        if (location != other.location) return false
        if (timestamp != other.timestamp) return false
        if (place != other.place) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}