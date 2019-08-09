package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.combah.travel2.R
import com.combah.travel2.databinding.FragmentTripBinding
import com.combah.travel2.extensions.observe
import com.combah.travel2.ui.trip.eventlist.TripEventsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject

class TripFragment : Fragment() {

    private val factory: TripViewModel.Factory by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        arguments?.let { factory.tripId = TripFragmentArgs.fromBundle(it).tripId }


        val viewModel = ViewModelProviders.of(this, factory)
                .get(TripViewModel::class.java)

        val binding = FragmentTripBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = TripEventsAdapter()
        binding.eventList.adapter = adapter

        viewModel.firstEvents.observe(this) { firstEvents ->
            firstEvents?.let {
                adapter.setFirstEvents(it)
            }
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.addPlanModal)
        bottomSheetBehavior.isHideable = true

        binding.addPlanButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.addTransportationTitle.setOnClickListener {
            navigateToAddPlan(binding.addPlanModal, R.id.action_tripFragment_to_transportationSetupFragment)
        }

        binding.addPlanClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        return binding.root
    }

    private fun navigateToAddPlan(modal: View, actionId: Int) {
        val extras = FragmentNavigator.Extras.Builder()
                .addSharedElement(modal, ViewCompat.getTransitionName(modal) ?: "")
                .build()

        findNavController()
                .navigate(actionId, null, null, extras)
    }


}
