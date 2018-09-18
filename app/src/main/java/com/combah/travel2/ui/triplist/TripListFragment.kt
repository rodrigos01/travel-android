package com.combah.travel2.ui.triplist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.combah.travel2.databinding.FragmentTripListBinding
import com.combah.travel2.di.ViewModelFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TripListFragment : DaggerFragment() {

    @Inject
    lateinit var factory: ViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentTripListBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(this)

        val viewModel = ViewModelProviders.of(this, factory)
            .get(TripListViewModel::class.java)

        val adapter = TripListAdapter(this, viewModel.trips)
        binding.tripList.adapter = adapter

        return binding.root
    }


}
