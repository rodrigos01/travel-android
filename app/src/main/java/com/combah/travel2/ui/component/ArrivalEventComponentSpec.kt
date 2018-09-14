package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.ui.data.ArrivalEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop

@LayoutSpec
object ArrivalEventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: ArrivalEvent
    ): Component = EventComponent.create(context)
        .iconResId(R.drawable.ic_flight_land_black_24dp)
        .titleRes(R.string.flight_arrival_tile)
        .subtitle(event.airport.name)
        .timestamp(event.timestamp)
        .build()
}