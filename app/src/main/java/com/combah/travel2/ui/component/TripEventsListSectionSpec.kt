package com.combah.travel2.ui.component

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.combah.travel2.ui.data.*
import com.facebook.litho.Component
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
    fun onRender(sectionContext: SectionContext, @FromEvent model: TripEvent): RenderInfo = ComponentRenderInfo.create()
        .component(getComponentForEvent(sectionContext, model))
        .build()

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

    private fun getComponentForEvent(sectionContext: SectionContext, event: TripEvent) = when (event) {
        is FlightEvent -> getFlightEventComponent(sectionContext, event)
        is ArrivalEvent -> getArrivalEvent(sectionContext, event)
        is CheckinEvent -> getCheckinEvent(sectionContext, event)
        is CheckoutEvent -> getCheckoutEvent(sectionContext, event)
        else -> EventComponent.create(sectionContext).build()
    }

    private fun getFlightEventComponent(sectionContext: SectionContext, event: FlightEvent): Component = FlightEventComponent.create(sectionContext)
        .event(event)
        .build()

    private fun getArrivalEvent(sectionContext: SectionContext, event: ArrivalEvent): Component = ArrivalEventComponent.create(sectionContext)
        .event(event)
        .build()

    private fun getCheckinEvent(sectionContext: SectionContext, event: CheckinEvent): Component = CheckinEventComponent.create(sectionContext)
        .event(event)
        .build()

    private fun getCheckoutEvent(sectionContext: SectionContext, event: CheckoutEvent): Component = CheckoutEventComponent.create(sectionContext)
        .event(event)
        .build()
}