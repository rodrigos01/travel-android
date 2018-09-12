package com.combah.travel2.ui.component

import com.combah.travel2.R
import com.combah.travel2.extensions.*
import com.combah.travel2.ui.data.FlightEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import java.util.*

@LayoutSpec
object FlightEventItemSpec {
    @OnCreateLayout
    fun onCreateLayout(
        context: ComponentContext,
        @Prop event: FlightEvent
    ): Component = Row.create(context)
        .children(
            Column.create(context)
                .marginDip(YogaEdge.ALL, 16f)
                .child(
                    Text.create(context)
                        .text(event.timestamp.asCalendar().get(Calendar.DAY_OF_MONTH).asString())
                        .textSizeSp(24f)
                )
                .child(
                    Text.create(context)
                        .text(event.timestamp.dayOfWeekString())
                        .textSizeSp(14f)
                ),
            Column.create(context)
                .marginDip(YogaEdge.ALL, 16f)
                .children(
                    Text.create(context)
                        .text(event.timestamp.timeString())
                        .textSizeSp(14f),
                    Row.create(context)
                        .marginDip(YogaEdge.TOP, 8f)
                        .children(
                            Image.create(context)
                                .drawableRes(R.drawable.ic_flight_takeoff_black_24dp),
                            Column.create(context)
                                .marginDip(YogaEdge.START, 16f)
                                .children(
                                    Text.create(context)
                                        .textRes(R.string.flight_event_tile, event.destination.name)
                                        .textSizeSp(18f),
                                    Text.create(context)
                                        .text(event.airport.name)
                                        .textSizeSp(14f)

                                )
                        )
                )
        )
        .build()
}