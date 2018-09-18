package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.ui.data.CheckinEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop

@LayoutSpec
object CheckinEventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: CheckinEvent
    ): Component = EventComponent.create(context)
        .iconRes(R.drawable.ic_hotel_black_24dp)
        .titleRes(R.string.hotel_checkin_title)
        .subtitle(event.hotel.name)
        .timestamp(event.timestamp)
        .build()
}