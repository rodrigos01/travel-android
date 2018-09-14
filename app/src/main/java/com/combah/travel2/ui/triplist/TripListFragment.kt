package com.combah.travel2.ui.triplist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.combah.travel2.di.ViewModelFactory
import com.combah.travel2.ui.component.TripListSection
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionComponent
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TripListFragment : DaggerFragment() {

    @Inject
    lateinit var factory: ViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val viewModel = ViewModelProviders.of(this, factory)
            .get(TripListViewModel::class.java)

        val componentContext = ComponentContext(context)
        val component = RecyclerCollectionComponent.create(componentContext)
            .section(TripListSection.create(SectionContext(componentContext))
                .tripsLiveData(viewModel.trips)
                .lifecycleOwner(this)
                .itemClickListener {
                    val action = TripListFragmentDirections.actionTripListFragmentToTripFragment(it.id)
                    findNavController().navigate(action)
                }
                .build())
            .build()
        return LithoView.create(componentContext, component)
    }


}
