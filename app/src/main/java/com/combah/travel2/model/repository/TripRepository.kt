package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Trip
import io.reactivex.Observable

interface TripRepository {
    val trips: Observable<List<Trip>>

    fun findTripById(tripId: String): Observable<Trip>
}