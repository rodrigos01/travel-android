package com.combah.travel2.ui.trip.creation


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.combah.travel2.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_add_plan.*

class AddPlanFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_add_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_plan_close.setOnClickListener {
            dismiss()
        }

        add_transportation_title.setOnClickListener {
            navigate(R.id.action_tripFragment_to_transportationSetupFragment)
        }
    }

    private fun navigate(actionId: Int) {
        val extras = FragmentNavigator.Extras.Builder()
                .addSharedElement(add_plan_modal, "frame")
                .build()

        findNavController()
                .navigate(actionId, null, null, extras)
    }
}
