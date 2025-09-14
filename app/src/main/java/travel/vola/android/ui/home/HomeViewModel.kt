package travel.vola.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.repository.UserPreferencesRepository
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import travel.vola.android.ui.triplist.TripListUseCase

class HomeViewModel private constructor(
    private val navController: NavController,
    private val tripListUseCase: TripListUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState> = tripListUseCase.state.map {
        UiState(tripListState = it)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = UiState(TripListUseCase.State(emptyList()))
    )

    data class UiState(
        val tripListState: TripListUseCase.State
    )

    fun addTrip() {
        viewModelScope.launch {
            val tripId = tripListUseCase.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }

    fun onDataSourceChanged(dataSourceType: DataSourceType) {
        viewModelScope.launch {
            userPreferencesRepository.setDataSource(dataSourceType)
        }
    }

    class Factory() :
        ViewModelProvider.Factory by viewModelFactory(initializer = {
            val tripListUseCase = TripListUseCase(factoryDependencies.tripRepository)
            HomeViewModel(
                factoryDependencies.navController,
                tripListUseCase,
                factoryDependencies.userPreferencesRepository,
            )
        })
}