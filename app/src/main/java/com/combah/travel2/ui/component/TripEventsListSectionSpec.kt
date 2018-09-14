package com.combah.travel2.ui.component

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.data.FlightEvent
import com.combah.travel2.ui.data.TripEvent
import com.facebook.litho.StateValue
import com.facebook.litho.annotations.*
import com.facebook.litho.sections.Children
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.annotations.GroupSectionSpec
import com.facebook.litho.sections.annotations.OnCreateChildren
import com.facebook.litho.sections.common.DataDiffSection
import com.facebook.litho.sections.common.RenderEvent
import com.facebook.litho.widget.ComponentRenderInfo
import com.facebook.litho.widget.RenderInfo

@GroupSectionSpec
object TripEventsListSectionSpec {

    @OnCreateChildren
    @JvmSuppressWildcards
    fun onCreateChildren(
        sectionContext: SectionContext,
        @State events: List<TripEvent>
    ) = Children.create()
        .child(DataDiffSection.create<TripEvent>(sectionContext)
            .data(events)
            .renderEventHandler(TripEventsListSection.onRender(sectionContext)))
        .build()

    @OnEvent(RenderEvent::class)
    fun onRender(sectionContext: SectionContext, @FromEvent model: TripEvent) = when (model) {
        is FlightEvent -> renderFlightEvent(sectionContext, model)
        is ArrivalEvent -> renderArrivalEvent(sectionContext, model)
        else -> ComponentRenderInfo.createEmpty()
    }

    @OnCreateInitialState
    fun createInitialEventList(
        sectionContext: SectionContext,
        events: StateValue<List<TripEvent>>,
        @Prop
        eventsLiveData: LiveData<List<TripEvent>>,
        @Prop
        lifecycleOwner: LifecycleOwner
    ) {
        events.set(emptyList())
        eventsLiveData.observe(lifecycleOwner, Observer {
            TripEventsListSection.updateEvents(sectionContext, it)
        })
    }

    @OnUpdateState
    fun updateEvents(events: StateValue<List<TripEvent>>, @Param newEvents: List<TripEvent>) {
        events.set(newEvents)
    }

    private fun renderFlightEvent(sectionContext: SectionContext, event: FlightEvent): RenderInfo = ComponentRenderInfo.create()
        .component(FlightEventComponent.create(sectionContext)
            .event(event)
            .build())
        .build()

    private fun renderArrivalEvent(sectionContext: SectionContext, event: ArrivalEvent): RenderInfo = ComponentRenderInfo.create()
        .component(ArrivalEventComponent.create(sectionContext)
            .event(event)
            .build())
        .build()
}