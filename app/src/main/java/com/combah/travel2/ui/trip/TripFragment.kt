package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.combah.travel2.databinding.FragmentTripBinding
import com.combah.travel2.ui.trip.eventlist.TripEventsAdapter
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TripFragment : Fragment() {

    private val tripId: String? by lazy { arguments?.let { TripFragmentArgs.fromBundle(it).tripId } }
    private val viewModel: TripViewModel by viewModel { parametersOf(tripId) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentTripBinding.inflate(inflater, container, false)

        val adapter = TripEventsAdapter(this, viewModel)
        binding.eventList.adapter = adapter

        return binding.root
    }


}
