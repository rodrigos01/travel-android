package com.combah.travel2.ui.trip


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.combah.travel2.R
import com.combah.travel2.databinding.FragmentTripBinding
import com.combah.travel2.ui.trip.eventlist.composable.EventList
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TripFragment : Fragment() {

    private val tripId: String? by lazy { arguments?.let { TripFragmentArgs.fromBundle(it).tripId } }
    private val viewModel: TripViewModel by viewModel { parametersOf(tripId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTripBinding.inflate(inflater, container, false)

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.addPlanModal)
        bottomSheetBehavior.isHideable = true

        binding.addPlanButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.addTransportationTitle.setOnClickListener {
            navigateToAddPlan(
                binding.addPlanModal,
                R.id.action_tripFragment_to_transportationSetupFragment
            )
        }

        binding.addPlanClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.composeContainer.setContent {
            MaterialTheme {
                val events by viewModel.events.observeAsState(emptyList())
                val firstEvents by viewModel.firstEvents.observeAsState()
                EventList(events = events, firstEvents = firstEvents ?: emptySet())
            }
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
