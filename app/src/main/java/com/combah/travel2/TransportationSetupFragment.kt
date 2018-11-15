package com.combah.travel2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import com.combah.travel2.databinding.FragmentTransportationSetupBinding
import com.combah.travel2.extensions.observe
import com.combah.travel2.ui.widget.makeDatePickerDialog
import dagger.android.support.DaggerFragment


class TransportationSetupFragment : DaggerFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = AutoTransition()
        sharedElementReturnTransition = AutoTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTransportationSetupBinding.inflate(inflater, container, false)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.inputDepartureDate.setOnClickListener {
            makeDatePickerDialog(it.context).observe(this) {

            }
        }

        return binding.root
    }


}
