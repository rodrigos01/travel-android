package com.combah.travel2

import com.combah.travel2.di.DaggerAppComponent
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.soloader.SoLoader
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class TravelApp : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()

        SoLoader.init(this, false)
        Fresco.initialize(this)
    }

    override fun applicationInjector(): AndroidInjector<DaggerApplication> {
        val component = DaggerAppComponent.builder()
            .application(this)
            .build()
        component.inject(this)

        return component
    }
}