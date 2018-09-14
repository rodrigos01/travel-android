package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.combah.travel2.ui.component.TripEventsListSection
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionComponent
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TripFragment : DaggerFragment() {

    @Inject
    lateinit var factory: TripViewModel.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val tripId = TripFragmentArgs.fromBundle(arguments).tripId

        factory.tripId = tripId
        val viewModel = ViewModelProviders.of(this, factory)
            .get(TripViewModel::class.java)

        val componentContext = ComponentContext(context)
        val component = RecyclerCollectionComponent.create(componentContext)
            .section(TripEventsListSection.create(SectionContext(componentContext))
                .eventsLiveData(viewModel.events)
                .lifecycleOwner(this)
                .build())
            .build()
        return LithoView.create(componentContext, component)
    }


}
