package com.combah.travel2.di

import com.combah.travel2.extensions.TimeConverter
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.ui.trip.creation.TransportationSetupViewModel
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import com.combah.travel2.ui.trip.viewmodel.TripViewModel
import com.combah.travel2.ui.triplist.TripListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TripListViewModel(get()) }
    viewModel { (tripId: String) ->
        TripViewModel(
            get(),
            tripId,
            AddPlanUseCase(TimeFormatter()),
            TimeConverter(),
            TimeFormatter(),
        )
    }
    viewModel { TransportationSetupViewModel() }
}