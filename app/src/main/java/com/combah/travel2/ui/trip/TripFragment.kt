package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.combah.travel2.databinding.FragmentTripBinding
import com.combah.travel2.ui.trip.eventlist.TripEventsAdapter
import org.koin.android.ext.android.inject

class TripFragment : Fragment() {

    private val factory: TripViewModel.Factory by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentTripBinding.inflate(inflater, container, false)

        arguments?.let {
            val tripId = TripFragmentArgs.fromBundle(it).tripId

            factory.tripId = tripId
            val viewModel = ViewModelProviders.of(this, factory)
                    .get(TripViewModel::class.java)


            val adapter = TripEventsAdapter(this, viewModel)
            binding.eventList.adapter = adapter
        }

        return binding.root
    }


}
