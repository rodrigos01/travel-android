package com.combah.travel2.ui.trip.eventlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.combah.travel2.databinding.EventListItemBinding
import com.combah.travel2.databinding.PlaceEventListItemBinding
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.trip.eventlist.viewmodel.*
import com.combah.travel2.ui.widget.ReactiveAdapter

class TripEventsAdapter(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<List<TripEvent>>
) : ReactiveAdapter<TripEvent, ViewDataBinding>(lifecycleOwner, liveData) {

    companion object {
        private const val VIEW_TYPE_PLACE_EVENT = 0
        private const val VIEW_TYPE_REGULAR_EVENT = 1
    }

    override fun getBinding(context: Context, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return when (viewType) {
            VIEW_TYPE_PLACE_EVENT -> getPlaceEventBinding(context, parent)
            else -> getRegularEventBinding(context, parent)
        }
    }

    override fun bind(binding: ViewDataBinding, item: TripEvent) {
        if (binding is PlaceEventListItemBinding) {
            bindPlaceEvent(binding, item as PlaceEvent)
        } else {
            bindRegularEvent(binding as EventListItemBinding, item)
        }
    }

    private fun getPlaceEventBinding(context: Context, parent: ViewGroup): PlaceEventListItemBinding {
        return PlaceEventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private fun getRegularEventBinding(context: Context, parent: ViewGroup): EventListItemBinding {
        return EventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private fun bindPlaceEvent(binding: PlaceEventListItemBinding, event: PlaceEvent) {
        binding.place = event.place
    }

    private fun bindRegularEvent(binding: EventListItemBinding, event: TripEvent) {
        binding.viewModel = when (event) {
            is FlightEvent -> FlightEventViewModel(event)
            is ArrivalEvent -> ArrivalEventViewModel(event)
            is CheckinEvent -> CheckinEventViewModel(event)
            is CheckoutEvent -> CheckoutEventViewModel(event)
            else -> EventListItemViewModel(event)
        }
    }

    override fun getItemViewType(item: TripEvent): Int {
        return when (item) {
            is PlaceEvent -> VIEW_TYPE_PLACE_EVENT
            else -> VIEW_TYPE_REGULAR_EVENT
        }
    }
}