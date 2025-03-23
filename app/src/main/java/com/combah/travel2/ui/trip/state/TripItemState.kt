package com.combah.travel2.ui.trip.state

import com.combah.travel2.model.data.Identifiable
import com.combah.travel2.model.data.Time

sealed interface TripItemState {

    interface Timeable {
        val timestamp: Time
    }

    data class MonthItemState(override val timestamp: Time, val month: String, val year: String) :
        TripItemState, Timeable

    data class PlaceItemState(
        override val timestamp: Time,
        val placeName: String,
        val imageUrl: String,
        val dateStart: String,
        val dateEnd: String,
    ) : TripItemState, Timeable

    data class DateRangeItemState(
        override val id: String,
        override val timestamp: Time,
        val dayOfMonthStart: String,
        val dayOfWeekStart: String,
        val dayOfMonthEnd: String,
        val dayOfWeekEnd: String,
    ) : TripItemState, Timeable, Replaceable

    interface Replaceable : Identifiable

    data class EmptyDateItemState(
        override val id: String,
        override val timestamp: Time,
        val dayOfMonth: String,
        val dayOfWeek: String,
    ) : TripItemState, Timeable, Replaceable

    sealed interface EventItemState : TripItemState, Timeable, Identifiable {
        val showDate: Boolean
        val dayOfMonth: String?
        val dayOfWeek: String?
        val time: String
        val title: String?
        val subtitle: String?
    }

    data class FlightDepartureItemState(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val destination: String,
        val airport: String
    ) : EventItemState, Replaceable {
        override val title = destination
        override val subtitle = airport
    }

    data class FlightArrivalItemState(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val airport: String
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = airport
    }

    data class HotelCheckInItemState(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val hotelName: String,
        val hotelAddress: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = hotelAddress
    }

    data class HotelCheckOutItemState(
        override val id: String,
        override val timestamp: Time,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        val hotelName: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = hotelName
    }

    data class EmptyAddPlanItemState(
        override val id: String,
        override val timestamp: Time,
        val showDivider: Boolean,
    ) : Replaceable, Timeable, TripItemState

    data class InitialAddPlanItemState(
        override val id: String,
        override val timestamp: Time,
    ) : Replaceable, Timeable, TripItemState
}

data class ManualAddPlanState(
    val time: Time?,
    val minTime: Time,
    val dateSelectionEnabled: Boolean,
    val locationText: String?,
    val searchResults: List<String>
)

sealed interface AddPlanItemState : TripItemState, Identifiable,
    TripItemState.Timeable {

    val typeSelectionEnabled: Boolean
    val dateSelectionEnabled: Boolean
    val saveButtonEnabled: Boolean
    val deleteButtonEnabled: Boolean

    val types: List<Type>
        get() = Type.entries

    enum class Type {
        Flight, Lodging
    }

}

sealed interface ManualStartEndAddPlanState : AddPlanItemState {
    val startState: ManualAddPlanState
    val endState: ManualAddPlanState
    override val dateSelectionEnabled: Boolean
        get() = startState.dateSelectionEnabled
}

sealed interface AddLodgingItemState : AddPlanItemState

data class ManualAddLodgingItemState(
    override val id: String,
    override val timestamp: Time,
    override val typeSelectionEnabled: Boolean,
    override val startState: ManualAddPlanState,
    override val endState: ManualAddPlanState,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
) : AddLodgingItemState, ManualStartEndAddPlanState

data class AddFlightItemState(
    override val id: String,
    override val timestamp: Time,
    override val typeSelectionEnabled: Boolean,
    override val startState: ManualAddPlanState,
    override val endState: ManualAddPlanState,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
) : ManualStartEndAddPlanState

data class LodgingSearchItemState(
    override val id: String,
    override val timestamp: Time,
    override val typeSelectionEnabled: Boolean,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
    override val dateSelectionEnabled: Boolean,
    val locationText: String?,
    val searchResults: List<SearchResultItemState>,
    val checkIn: Time?,
    val minCheckOutTime: Time?,
    val checkOut: Time?,
) : AddLodgingItemState

data class SearchResultItemState(val title: String, val subtitle: String)

val AddPlanItemState.type
    get() = when (this) {
        is AddFlightItemState -> AddPlanItemState.Type.Flight
        is ManualAddLodgingItemState,
        is LodgingSearchItemState -> AddPlanItemState.Type.Lodging
    }
