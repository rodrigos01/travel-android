package com.combah.travel2.ui.component

import com.combah.travel2.ui.data.PlaceEvent
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.fresco.FrescoImage

@LayoutSpec
object PlaceEventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: PlaceEvent
    ): Component = FrescoImage.create(context)
        .controller(Fresco.newDraweeControllerBuilder()
            .setUri(event.place.coverImage)
            .build())
        .actualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
        .imageAspectRatio(2.334f)
        .build()
}