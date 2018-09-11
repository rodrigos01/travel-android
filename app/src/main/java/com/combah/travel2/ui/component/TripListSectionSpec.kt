package com.combah.travel2.ui.component

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.combah.travel2.model.data.Trip
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
class TripListSectionSpec {
    companion object {
        @JvmStatic
        @OnCreateChildren
        fun onCreateChildren(
            sectionContext: SectionContext,
            @State trips: List<Trip>
        ) = Children.create()
            .child(DataDiffSection.create<Trip>(sectionContext)
                .data(trips)
                .renderEventHandler(TripListSection.onRender(sectionContext)))
            .build()

        @JvmStatic
        @OnEvent(RenderEvent::class)
        fun onRender(sectionContext: SectionContext, @FromEvent model: Trip): RenderInfo = ComponentRenderInfo.create()
            .component(TripListItem.create(sectionContext)
                .name(model.name ?: "")
                .imageUrl(model.coverImage ?: "")
                .build())
            .build()

        @JvmStatic
        @OnCreateInitialState
        fun createInitialTripList(sectionContext: SectionContext, trips: StateValue<List<Trip>>, @Prop tripsLiveData: LiveData<List<Trip>>, @Prop lifecycleOwner: LifecycleOwner) {
            trips.set(emptyList())
            tripsLiveData.observe(lifecycleOwner, Observer {
                TripListSection.updateTrips(sectionContext, it)
            })
        }

        @JvmStatic
        @OnUpdateState
        fun updateTrips(trips: StateValue<List<Trip>>, @Param newTrips: List<Trip>) {
            trips.set(newTrips)
        }
    }
}