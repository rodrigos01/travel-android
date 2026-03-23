package travel.vola.android.ui.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination

class TripListViewModel(
    private val navController: NavController,
    private val tripListUseCase: TripListUseCase,
) : ViewModel() {

    val viewState = tripListUseCase.state.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = TripListUseCase.State(emptyList()),
    )

    fun addTrip() {
        viewModelScope.launch {
            val tripId = tripListUseCase.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }

    class Factory() :
        ViewModelProvider.Factory by viewModelFactory(initializer = {
            val tripListUseCase = TripListUseCase(factoryDependencies.tripRepository)
            TripListViewModel(factoryDependencies.navController, tripListUseCase)
        })
}
