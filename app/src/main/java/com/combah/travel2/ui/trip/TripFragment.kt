package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.combah.travel2.databinding.FragmentTripBinding
import com.combah.travel2.ui.trip.eventlist.TripEventsAdapter
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

        val binding = FragmentTripBinding.inflate(inflater, container, false)

        val adapter = TripEventsAdapter(this, viewModel.events)
        binding.eventList.adapter = adapter

        return binding.root
    }


}
