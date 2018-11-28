package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.data.Trip
import io.reactivex.Observable

interface TripRepository {
    val trips: Observable<List<Trip>>

    fun findTripById(tripId: String): Observable<Trip>
    fun getTripFlights(tripId: String): Observable<List<Flight>>
    fun getTripHotels(tripId: String): Observable<List<Hotel>>
}