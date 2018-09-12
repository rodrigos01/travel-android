package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.extensions.children
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.Text

@LayoutSpec
object TripListItemSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop imageUrl: String,
        @Prop name: String
    ): Component = Column.create(context)
        .children(
            FrescoImage.create(context)
                .controller(Fresco.newDraweeControllerBuilder()
                    .setUri(imageUrl)
                    .build())
                .actualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .placeholderImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .imageAspectRatio(1.778f)
                .placeholderImageRes(R.drawable.ic_launcher_background),
            Text.create(context)
                .textSizeDip(24f)
                .text(name)
        )
        .build()
}