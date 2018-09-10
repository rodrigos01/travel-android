package com.combah.travel2.di

import androidx.lifecycle.ViewModel
import com.combah.travel2.ui.TripViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(TripViewModel::class)
    abstract fun bindTripListViewModel(tripListViewModel: TripViewModel): ViewModel
}