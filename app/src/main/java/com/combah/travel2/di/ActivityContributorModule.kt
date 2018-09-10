package com.combah.travel2.di

import com.combah.travel2.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityContributorModule {
    @ContributesAndroidInjector
    abstract fun constributeMainActivity(): MainActivity
}