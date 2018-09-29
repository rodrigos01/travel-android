package com.combah.travel2

import com.combah.travel2.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class TravelApp : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<DaggerApplication> {
        val component = DaggerAppComponent.builder()
                .application(this)
                .build()
        component.inject(this)

        return component
    }
}