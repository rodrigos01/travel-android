package com.combah.travel2.di

import com.combah.travel2.ui.trip.creation.TransportationSetupFragment
import com.combah.travel2.ui.MainActivity
import com.combah.travel2.ui.trip.TripFragment
import com.combah.travel2.ui.triplist.TripListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentContributorModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeTripListFragment(): TripListFragment

    @ContributesAndroidInjector
    abstract fun contributeTripFragment(): TripFragment

    @ContributesAndroidInjector
    abstract fun contributeTransportationSetupFragment(): TransportationSetupFragment
}