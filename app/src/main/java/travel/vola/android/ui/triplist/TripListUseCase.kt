package travel.vola.android.ui.triplist

import kotlinx.coroutines.flow.map
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripRepository

class TripListUseCase(private val repository: TripRepository) {

    data class State(
        val trips: List<Trip>
    )

    val state = repository.trips.map {
        State(trips = it)
    }

    suspend fun addTrip(): String {
        return repository.addTrip()
    }
}