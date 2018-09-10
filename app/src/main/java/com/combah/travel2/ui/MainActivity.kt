package com.combah.travel2.ui

import android.os.Bundle
import com.combah.travel2.ui.component.FlightEventItem
import com.combah.travel2.ui.data.Event
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import dagger.android.support.DaggerAppCompatActivity
import java.util.*

class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val componentContext = ComponentContext(this)

        val component = FlightEventItem.create(componentContext)
            .event(Event(
                "Paris",
                "John F Kennedy International Airport",
                Date()
            ))
            .build()
        setContentView(LithoView.create(componentContext, component))

    }
}
