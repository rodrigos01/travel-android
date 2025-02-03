package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Time

sealed interface TripItem {

    interface Timeable {
        val timestamp: Time
    }

    data class MonthItem(override val timestamp: Time, val month: String, val year: String) :
        TripItem, Timeable

    data class PlaceItem(
        override val timestamp: Time,
        val placeName: String,
        val imageUrl: String,
        val dateStart: String,
        val dateEnd: String,
    ) : TripItem, Timeable

    interface Identifiable {
        val id: String
    }

    data class DateRangeItem(
        override val id: String,
        override val timestamp: Time,
        val dayOfMonthStart: String,
        val dayOfWeekStart: String,
        val dayOfMonthEnd: String,
        val dayOfWeekEnd: String,
    ) : TripItem, Timeable, Identifiable

    interface Replaceable

    data class EmptyDateItem(
        override val id: String,
        override val timestamp: Time,
        val dayOfMonth: String,
        val dayOfWeek: String,
    ) : TripItem, Timeable, Identifiable, Replaceable

    sealed interface EventItem : TripItem, Timeable, Identifiable {
        val showDate: Boolean
        val dayOfMonth: String?
        val dayOfWeek: String?
        val time: String
        val title: String?
        val subtitle: String?
    }

    data class FlightDepartureItem(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val destination: String,
        val airport: String
    ) : EventItem {
        override val title = destination
        override val subtitle = airport
    }

    data class FlightArrivalItem(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val airport: String
    ) : EventItem {
        override val title = null
        override val subtitle = airport
    }

    data class HotelCheckInItem(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val hotelName: String,
        val hotelAddress: String,
    ) : EventItem {
        override val title = null
        override val subtitle = hotelAddress
    }

    data class HotelCheckOutItem(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val hotelName: String,
    ) : EventItem {
        override val title = null
        override val subtitle = hotelName
    }

    data class EmptyAddPlanItem(
        override val id: String,
        override val timestamp: Time,
        val showDivider: Boolean,
    ) : Replaceable, Timeable, Identifiable, TripItem

    data class InitialAddPlanItem(
        override val id: String,
        override val timestamp: Time,
    ) : Replaceable, Timeable, Identifiable, TripItem
}
