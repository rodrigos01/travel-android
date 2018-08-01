package com.combah.travel2

import android.app.Application
import com.facebook.soloader.SoLoader

class TravelApp : Application() {
    override fun onCreate() {
        super.onCreate()

        SoLoader.init(this, false)
    }
}