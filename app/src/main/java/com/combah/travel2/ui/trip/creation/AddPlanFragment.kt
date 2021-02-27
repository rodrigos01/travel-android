package com.combah.travel2.ui.trip.creation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.combah.travel2.R
import com.combah.travel2.databinding.FragmentAddPlanBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPlanFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentAddPlanBinding

    private val addPlanClose: View by lazy { _binding.addPlanClose }
    private val addTransportationTitle: View by lazy { _binding.addTransportationTitle }
    private val addPlanModal: View by lazy { _binding.addPlanModal }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddPlanBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addPlanClose.setOnClickListener {
            dismiss()
        }

        addTransportationTitle.setOnClickListener {
            navigate(R.id.action_tripFragment_to_transportationSetupFragment)
        }
    }

    private fun navigate(actionId: Int) {
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(addPlanModal, "frame")
            .build()

        findNavController()
            .navigate(actionId, null, null, extras)
    }
}
