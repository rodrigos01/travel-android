package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.ui.data.CheckoutEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop

@LayoutSpec
object CheckoutEventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: CheckoutEvent
    ): Component = EventComponent.create(context)
        .iconRes(R.drawable.ic_hotel_black_24dp)
        .titleRes(R.string.hotel_checkout_title)
        .subtitle(event.hotelName)
        .timestamp(event.timestamp)
        .build()
}