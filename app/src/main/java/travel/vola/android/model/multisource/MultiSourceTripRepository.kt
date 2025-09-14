package travel.vola.android.model.multisource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripDataSource
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.repository.UserPreferencesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MultiSourceTripRepository(
    userPreferencesRepository: UserPreferencesRepository,
    private vararg val dataSources: TripDataSource
) : TripRepository {

    private val currentDataSourceType =
        userPreferencesRepository.preferences.map { it.dataSourceType }.stateIn(
            CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = DataSourceType.LOCAL,
        )
    private val currentDataSource
        get() = currentDataSourceType.value.dataSource

    override val trips: Flow<List<Trip>> = currentDataSourceType.flatMapLatest { dataSourceType ->
        dataSourceType.dataSource.trips
    }

    override fun findTripById(tripId: String): Flow<Trip> = currentDataSource.findTripById(tripId)

    override fun getTripFlights(tripId: String): Flow<List<Flight>> =
        currentDataSource.getTripFlights(tripId)

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> =
        currentDataSource.getTripHotels(tripId)

    override suspend fun addTrip(): String = currentDataSource.addTrip()

    override suspend fun updateName(tripId: String, newName: String) =
        currentDataSource.updateName(tripId, newName)

    override suspend fun deleteTrip(tripId: String) = currentDataSource.deleteTrip(tripId)

    override suspend fun saveFlight(
        tripId: String, flight: Flight
    ) = currentDataSource.saveFlight(tripId, flight)

    override suspend fun saveLodging(
        tripId: String, lodging: Lodging
    ) = currentDataSource.saveLodging(tripId, lodging)

    override suspend fun saveTimedPlace(
        tripId: String, timedPlace: TimedPlace
    ) = currentDataSource.saveTimedPlace(tripId, timedPlace)

    override suspend fun deleteFlight(tripId: String, flightId: String) =
        currentDataSource.deleteFlight(tripId, flightId)

    override suspend fun deleteLodging(tripId: String, lodgingId: String) =
        currentDataSource.deleteLodging(tripId, lodgingId)

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) {
        currentDataSource.deleteTimedPlace(tripId, timedPlaceId)
    }

    private val DataSourceType.dataSource: TripDataSource
        get() = dataSources.first { it.dataSourceType == this }
}