package com.combah.travel2.ui.trip.eventlist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.combah.travel2.databinding.EventListItemBinding
import com.combah.travel2.databinding.MontEventListItemBinding
import com.combah.travel2.databinding.PlaceEventListItemBinding
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.trip.TripViewModel
import com.combah.travel2.ui.trip.eventlist.viewmodel.*
import com.combah.travel2.ui.widget.ReactiveAdapter

class TripEventsAdapter(
        lifecycleOwner: LifecycleOwner,
        viewModel: TripViewModel
) : ReactiveAdapter<TripEvent, ViewDataBinding>(lifecycleOwner, viewModel.events) {

    companion object {
        private const val VIEW_TYPE_MONTH_EVENT = 0
        private const val VIEW_TYPE_PLACE_EVENT = 1
        private const val VIEW_TYPE_REGULAR_EVENT = 2
    }

    private var firstEvents: Set<TripEvent>? = null

    init {
        viewModel.firstEvents.observe(lifecycleOwner, Observer {
            firstEvents = it
            notifyDataSetChanged()
        })
    }

    override fun getBinding(context: Context, parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_MONTH_EVENT -> getMonthEventBinding(context, parent)
        VIEW_TYPE_PLACE_EVENT -> getPlaceEventBinding(context, parent)
        else -> getRegularEventBinding(context, parent)
    }

    override fun bind(binding: ViewDataBinding, item: TripEvent) = when (binding) {
        is MontEventListItemBinding -> bindMonthEvent(binding, item as MonthEvent)
        is PlaceEventListItemBinding -> bindPlaceEvent(binding, item as PlaceEvent)
        else -> bindRegularEvent(binding as EventListItemBinding, item)
    }

    private fun getMonthEventBinding(context: Context, parent: ViewGroup): MontEventListItemBinding {
        return MontEventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private fun getPlaceEventBinding(context: Context, parent: ViewGroup): PlaceEventListItemBinding {
        return PlaceEventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private fun getRegularEventBinding(context: Context, parent: ViewGroup): EventListItemBinding {
        return EventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    private fun bindMonthEvent(binding: MontEventListItemBinding, event: MonthEvent) {
        binding.viewModel = MonthEventViewModel(event)
    }

    private fun bindPlaceEvent(binding: PlaceEventListItemBinding, event: PlaceEvent) {
        binding.place = event.place
    }

    private fun bindRegularEvent(binding: EventListItemBinding, event: TripEvent) {
        binding.viewModel = getViewModelForEvent(event)
    }

    private fun getViewModelForEvent(event: TripEvent) = when (event) {
        is FlightEvent -> FlightEventViewModel(event, event.isFirst())
        is ArrivalEvent -> ArrivalEventViewModel(event, event.isFirst())
        is CheckinEvent -> CheckinEventViewModel(event, event.isFirst())
        is CheckoutEvent -> CheckoutEventViewModel(event, event.isFirst())
        else -> EventListItemViewModel(event)
    }

    private fun TripEvent.isFirst() = firstEvents?.contains(this) ?: true

    override fun getItemViewType(item: TripEvent): Int {
        return when (item) {
            is MonthEvent -> VIEW_TYPE_MONTH_EVENT
            is PlaceEvent -> VIEW_TYPE_PLACE_EVENT
            else -> VIEW_TYPE_REGULAR_EVENT
        }
    }
}