package travel.vola.android.model.repository.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Trip
import travel.vola.android.model.firebase.toAppDataModel
import travel.vola.android.model.repository.TripRepository

class MockTripRepository : TripRepository {
    override val trips: Flow<List<Trip>>
        get() = flowOf(MockData.tripList.map { it.toAppDataModel() })

    override fun findTripById(tripId: String): Flow<Trip> = flowOf(MockData.trip.toAppDataModel())

    override fun getTripFlights(tripId: String): Flow<List<Flight>> =
        flowOf(MockData.trip.flights.map { it.toAppDataModel() })

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> =
        flowOf(MockData.trip.lodgings.map { it.toAppDataModel() })

    override suspend fun saveFlight(tripId: String, flight: Flight) = Unit

    override suspend fun saveLodging(tripId: String, lodging: Lodging) = Unit

    override suspend fun addTrip(): String = ""

    override suspend fun updateName(tripId: String, newName: String) = Unit

    override suspend fun deleteTrip(tripId: String) = Unit

    override suspend fun deleteFlight(tripId: String, flightId: String) = Unit

    override suspend fun deleteLodging(tripId: String, lodgingId: String) = Unit
}
