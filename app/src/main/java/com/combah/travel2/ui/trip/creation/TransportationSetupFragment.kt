package com.combah.travel2.ui.trip.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
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

        val viewModel = ViewModelProviders.of(this)
                .get(TransportationSetupViewModel::class.java)

        val binding = FragmentTransportationSetupBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }


}
