package com.combah.travel2.ui.component

import com.combah.travel2.extensions.*
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.ResType
import com.facebook.yoga.YogaEdge
import java.util.*

@LayoutSpec
object EventComponentSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop iconResId: Int,
        @Prop(resType = ResType.STRING) title: CharSequence,
        @Prop(resType = ResType.STRING) subtitle: CharSequence,
        @Prop timestamp: Date
    ): Component = com.facebook.litho.Row.create(context)
        .children(
            com.facebook.litho.Column.create(context)
                .marginDip(YogaEdge.ALL, 16f)
                .child(
                    com.facebook.litho.widget.Text.create(context)
                        .text(timestamp.asCalendar().get(Calendar.DAY_OF_MONTH).asString())
                        .textSizeSp(24f)
                )
                .child(
                    com.facebook.litho.widget.Text.create(context)
                        .text(timestamp.dayOfWeekString())
                        .textSizeSp(14f)
                ),
            com.facebook.litho.Column.create(context)
                .marginDip(YogaEdge.ALL, 16f)
                .children(
                    com.facebook.litho.widget.Text.create(context)
                        .text(timestamp.timeString())
                        .textSizeSp(14f),
                    com.facebook.litho.Row.create(context)
                        .marginDip(YogaEdge.TOP, 8f)
                        .children(
                            com.facebook.litho.widget.Image.create(context)
                                .drawableRes(iconResId),
                            com.facebook.litho.Column.create(context)
                                .marginDip(YogaEdge.START, 16f)
                                .children(
                                    com.facebook.litho.widget.Text.create(context)
                                        .text(title)
                                        .textSizeSp(18f),
                                    com.facebook.litho.widget.Text.create(context)
                                        .text(subtitle)
                                        .textSizeSp(14f)

                                )
                        )
                )
        )
        .build()
}