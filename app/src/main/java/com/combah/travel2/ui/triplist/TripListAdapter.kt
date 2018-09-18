package com.combah.travel2.ui.triplist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.combah.travel2.databinding.TripCardBinding
import com.combah.travel2.model.data.Trip
import com.combah.travel2.ui.widget.ReactiveAdapter

class TripListAdapter(
    owner: LifecycleOwner,
    liveData: LiveData<List<Trip>>
) : ReactiveAdapter<Trip, TripCardBinding>(owner, liveData) {

    override fun getBinding(context: Context, parent: ViewGroup, viewType: Int) = TripCardBinding
        .inflate(LayoutInflater.from(context), parent, false)

    override fun bind(binding: TripCardBinding, item: Trip) {
        binding.trip = item
    }
}