package travel.vola.android.model.repository

import kotlinx.coroutines.flow.Flow
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Trip

interface TripDataSource {

    val trips: Flow<List<Trip>>

    fun findTripById(tripId: String): Flow<Trip?>
    fun getTripFlights(tripId: String): Flow<List<Flight>>
    fun getTripHotels(tripId: String): Flow<List<Lodging>>
}
