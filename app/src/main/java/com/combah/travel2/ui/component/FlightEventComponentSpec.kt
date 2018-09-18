package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.ui.data.FlightEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop

@LayoutSpec
object FlightEventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: FlightEvent
    ): Component = EventComponent.create(context)
        .iconRes(R.drawable.ic_flight_takeoff_black_24dp)
        .titleRes(R.string.flight_event_tile, event.destination.name)
        .subtitle(event.airport.name)
        .timestamp(event.departure)
        .build()
}