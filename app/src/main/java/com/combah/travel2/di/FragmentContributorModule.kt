package com.combah.travel2.di

import com.combah.travel2.ui.MainActivity
import com.combah.travel2.ui.TripListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentContributorModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeTripListFragment(): TripListFragment
}