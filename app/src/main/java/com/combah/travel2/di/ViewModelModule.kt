package com.combah.travel2.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.ui.TripListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(TripListViewModel::class)
    abstract fun bindTripListViewModel(tripListViewModel: TripListViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}