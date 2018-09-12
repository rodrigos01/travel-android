package com.combah.travel2.ui.component

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.combah.travel2.model.data.Trip
import com.facebook.litho.ClickEvent
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
object TripListSectionSpec {

    @OnCreateChildren
    fun onCreateChildren(
        sectionContext: SectionContext,
        @State trips: List<Trip>
    ) = Children.create()
        .child(DataDiffSection.create<Trip>(sectionContext)
            .data(trips)
            .renderEventHandler(TripListSection.onRender(sectionContext)))
        .build()

    @OnEvent(RenderEvent::class)
    fun onRender(sectionContext: SectionContext, @FromEvent model: Trip): RenderInfo = ComponentRenderInfo.create()
        .component(TripListItem.create(sectionContext)
            .name(model.name ?: "")
            .imageUrl(model.coverImage ?: "")
            .clickHandler(TripListSection.onItemClick(sectionContext, model))
            .build())
        .build()

    @OnCreateInitialState
    fun createInitialTripList(
        sectionContext: SectionContext,
        trips: StateValue<List<Trip>>,
        @Prop
        tripsLiveData: LiveData<List<Trip>>,
        @Prop
        lifecycleOwner: LifecycleOwner
    ) {
        trips.set(emptyList())
        tripsLiveData.observe(lifecycleOwner, Observer {
            TripListSection.updateTrips(sectionContext, it)
        })
    }

    @OnUpdateState
    fun updateTrips(trips: StateValue<List<Trip>>, @Param newTrips: List<Trip>) {
        trips.set(newTrips)
    }

    @OnEvent(ClickEvent::class)
    fun onItemClick(
        sectionContext: SectionContext,
        @Param trip: Trip,
        @FromEvent view: View,
        @Prop
        itemClickListener: ((Trip) -> Unit)?
    ) {
        itemClickListener?.invoke(trip)
    }
}