package travel.vola.android.ui.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.vola.android.model.data.Trip
import com.vola.android.model.repository.TripRepository
import com.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripListViewModel(
    private val repository: TripRepository,
    private val navController: NavController,
) : ViewModel() {
    data class ViewState(
        val trips: List<Trip>
    )

    val viewState = repository.trips.map {
        ViewState(trips = it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = ViewState(emptyList()))

    fun addTrip() {
        viewModelScope.launch {
            val tripId = repository.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }
}
