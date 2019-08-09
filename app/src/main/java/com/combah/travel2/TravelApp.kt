package com.combah.travel2

import android.app.Application
import com.combah.travel2.di.repositoryModule
import com.combah.travel2.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TravelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TravelApp)
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}