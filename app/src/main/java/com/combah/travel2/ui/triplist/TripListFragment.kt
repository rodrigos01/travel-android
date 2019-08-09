package com.combah.travel2.ui.triplist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.combah.travel2.databinding.FragmentTripListBinding
import org.koin.android.viewmodel.ext.android.viewModel
import com.combah.travel2.databinding.TripListItemBinding
import com.combah.travel2.model.data.Trip
import com.combah.travel2.ui.widget.createAdapter

class TripListFragment : Fragment() {

    private val viewModel: TripListViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = FragmentTripListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = createAdapter<Trip, TripListItemBinding> { itemBinding, trip ->
            itemBinding.trip = trip
        }
        adapter.onItemClicked.observe(this, Observer {
            val action = TripListFragmentDirections.actionTripListFragmentToTripFragment(it.id)
            findNavController().navigate(action)
        })

        binding.tripList.adapter = adapter

        return binding.root
    }


}
