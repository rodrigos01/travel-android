package com.combah.travel2.ui.trip.creation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class TransportationSetupViewModel : ViewModel() {
    val departureDate = MutableLiveData<Date>()
    val returnDate = MutableLiveData<Date>()
}