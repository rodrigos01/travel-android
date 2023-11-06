package com.combah.travel2.ui.trip.creation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class TransportationSetupViewModel : ViewModel() {

    data class ViewState(
        val from: String?,
        val to: String?,
        val departureDate: Date?,
        val arrivalDate: Date?,
    )

    private val _viewState: MutableStateFlow<ViewState> =
        MutableStateFlow(ViewState(null, null, null, null))
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    fun setFromLocation(from: String) {
        _viewState.value = viewState.value.copy(from = from)
    }

    fun setToLocation(to: String) {
        _viewState.value = viewState.value.copy(to = to)
    }

    fun setDepartureDate(departure: Date?) {
        _viewState.value = viewState.value.copy(departureDate = departure)
    }

    fun setArrivalDate(arrival: Date?) {
        _viewState.value = viewState.value.copy(arrivalDate = arrival)
    }
}