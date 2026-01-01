package travel.vola.android.ui.trip.state

import travel.vola.android.model.data.Identifiable
import java.time.ZonedDateTime

sealed interface TripItemState {

    val timestamp: ZonedDateTime

    interface Timeable

    interface SectionItemState {
        val sectionId: String?
    }

    data class MonthItemState(
        override val timestamp: ZonedDateTime,
        override val sectionId: String? = null, val month: String, val year: String,
    ) :
        TripItemState, Timeable, SectionItemState

    data class PlaceItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val sectionId: String? = null,
        val placeName: String,
        val imageUrl: String,
        val dateStart: String,
        val dateEnd: String,
    ) : TripItemState, Timeable, Editable, Replaceable, SectionItemState

    data class DateRangeItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val sectionId: String? = null,
        val dayOfMonthStart: String,
        val dayOfWeekStart: String,
        val dayOfMonthEnd: String,
        val dayOfWeekEnd: String,
        val isGeneratingPlans: Boolean,
    ) : TripItemState, Timeable, Replaceable, SectionItemState, Focusable {
        override val showDate: Boolean = true
    }

    interface Replaceable : Identifiable

    sealed interface Editable : Identifiable

    sealed interface Focusable : TripItemState, Identifiable, Timeable {
        val showDate: Boolean
    }

    data class EmptyDateItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val sectionId: String? = null,
        val dayOfMonth: String,
        val dayOfWeek: String,
        val isGeneratingPlans: Boolean,
    ) : TripItemState, Timeable, Replaceable, SectionItemState, Focusable {
        override val showDate: Boolean = true
    }

    interface EventWithDateState {
        val dayOfMonth: String?
        val dayOfWeek: String?
    }

    sealed interface EventItemState : TripItemState, Timeable, Editable, SectionItemState,
        Focusable, EventWithDateState {
        val time: String
        val title: String?
        val subtitle: String?
        val backgroundStyle: BackgroundStyle

        enum class BackgroundStyle {
            TOP, MIDDLE, BOTTOM, SINGLE
        }
    }

    data class FlightDepartureItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val destination: String,
        val airport: String,
    ) : EventItemState, Replaceable {
        override val title = destination
        override val subtitle = airport
    }

    data class FlightArrivalItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val airport: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = airport
    }

    data class HotelCheckInItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val hotelName: String,
        val hotelAddress: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = hotelAddress
    }

    data class HotelCheckOutItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val hotelName: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = hotelName
    }

    data class TimedPlaceItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val time: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val showTime: Boolean,
        val placeName: String,
        val cityName: String,
        val imageUrl: String,
    ) : EventItemState, Replaceable {
        override val title = placeName
        override val subtitle = cityName
    }

    data class RestaurantReservationItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val showDate: Boolean,
        override val dayOfMonth: String,
        override val dayOfWeek: String,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        override val time: String,
        val restaurantName: String,
        val restaurantAddress: String,
    ) : EventItemState, Replaceable {
        override val title = null
        override val subtitle = restaurantAddress
    }

    data class InitialAddPlanItemState(
        override val id: String,
        override val timestamp: ZonedDateTime,
    ) : Replaceable, Timeable, TripItemState

    data class FlexibleDaySectionState(
        override val id: String,
        override val timestamp: ZonedDateTime,
        override val dayOfMonth: String?,
        override val dayOfWeek: String?,
        override val showDate: Boolean,
        override val backgroundStyle: EventItemState.BackgroundStyle = EventItemState.BackgroundStyle.MIDDLE,
        override val sectionId: String? = null,
        val name: String,
        override val subtitle: String,
        val categories: List<DaySectionCategory>,
        val searchResults: List<SearchResultItemState>,
        val isGenerated: Boolean = false,
    ) : EventItemState, EventWithDateState, Focusable, Replaceable {
        override val time: String = ""
        override val title: String = name
    }

    data class DaySectionCategory(
        val name: String,
        val items: List<SectionOption>,
    )

    data class SectionOption(
        val id: String,
        val title: String,
        val subtitle: String,
        val imageUrl: String,
    )

    data class SuggestionPlaceholderItemState(
        override val timestamp: ZonedDateTime,
        override val backgroundStyle: EventItemState.BackgroundStyle,
        override val sectionId: String?,
        override val showDate: Boolean,
        override val dayOfMonth: String?,
        override val dayOfWeek: String?,
    ) : EventItemState {
        override val id: String = timestamp.toString()
        override val time: String = ""
        override val title: String? = null
        override val subtitle: String? = null
    }
}

data class ManualAddPlanState(
    val dateTime: ZonedDateTime?,
    val minDateTime: ZonedDateTime?,
    val isTimeSet: Boolean,
    val dateSelectionEnabled: Boolean,
    val locationText: String?,
    val searchResults: List<AutoCompleteResultState>,
)

sealed interface AddPlanItemState : TripItemState, Identifiable, TripItemState.Timeable {

    val typeSelectionEnabled: Boolean
    val dateSelectionEnabled: Boolean
    val saveButtonEnabled: Boolean
    val deleteButtonEnabled: Boolean
    val buttonConfiguration: ButtonConfiguration
        get() = ButtonConfiguration.Save

    val types: List<Type>
        get() = Type.entries

    enum class Type {
        Flight, Lodging, Place, Restaurant, FlexibleSection,
    }

    enum class ButtonConfiguration {
        Save, Search,
    }

}

sealed interface ManualStartEndAddPlanState : AddPlanItemState {
    val startState: ManualAddPlanState
    val endState: ManualAddPlanState
    override val dateSelectionEnabled: Boolean
        get() = startState.dateSelectionEnabled
}

sealed interface AddLodgingItemState : AddPlanItemState {
    val checkIn: ZonedDateTime?
    val checkOut: ZonedDateTime?
}

data class AutoCompleteResultState(val title: String, val subtitle: String?)

data class ManualAddLodgingItemState(
    override val id: String,
    override val timestamp: ZonedDateTime,
    override val typeSelectionEnabled: Boolean,
    override val startState: ManualAddPlanState,
    override val endState: ManualAddPlanState,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
) : AddLodgingItemState, ManualStartEndAddPlanState {
    override val checkIn: ZonedDateTime? = startState.dateTime
    override val checkOut: ZonedDateTime? = endState.dateTime
}

data class AddFlightItemState(
    override val id: String,
    override val timestamp: ZonedDateTime,
    override val typeSelectionEnabled: Boolean,
    override val startState: ManualAddPlanState,
    override val endState: ManualAddPlanState,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
) : ManualStartEndAddPlanState

data class LodgingSearchItemState(
    override val id: String,
    override val timestamp: ZonedDateTime,
    override val typeSelectionEnabled: Boolean,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
    override val dateSelectionEnabled: Boolean,
    val locationText: String?,
    val searchResults: List<SearchResultItemState>,
    override val checkIn: ZonedDateTime,
    val minCheckOutTime: ZonedDateTime?,
    override val checkOut: ZonedDateTime?,
) : AddLodgingItemState {
    override val buttonConfiguration: AddPlanItemState.ButtonConfiguration =
        AddPlanItemState.ButtonConfiguration.Search
}

data class AddPlaceItemState(
    override val id: String,
    override val typeSelectionEnabled: Boolean,
    override val dateSelectionEnabled: Boolean,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
    override val timestamp: ZonedDateTime,
    val startTimeSelected: Boolean,
    val endDateTime: ZonedDateTime?,
    val endTimeSelected: Boolean,
    val minEndTime: ZonedDateTime?,
    val placeName: String?,
    val searchResults: List<AutoCompleteResultState>,
) : ManualStartEndAddPlanState {
    override val startState: ManualAddPlanState = ManualAddPlanState(
        dateTime = timestamp,
        minDateTime = null,
        isTimeSet = startTimeSelected,
        dateSelectionEnabled = dateSelectionEnabled,
        locationText = placeName,
        searchResults = searchResults,
    )
    override val endState: ManualAddPlanState = ManualAddPlanState(
        dateTime = endDateTime,
        minDateTime = minEndTime,
        isTimeSet = endTimeSelected,
        dateSelectionEnabled = dateSelectionEnabled,
        locationText = null,
        searchResults = emptyList(),
    )
}

data class AddRestaurantItemState(
    override val id: String,
    override val typeSelectionEnabled: Boolean,
    override val dateSelectionEnabled: Boolean,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
    override val timestamp: ZonedDateTime,
    val timeSelected: Boolean,
    val restaurantName: String?,
    val searchResults: List<AutoCompleteResultState>,
) : ManualStartEndAddPlanState {
    override val startState: ManualAddPlanState = ManualAddPlanState(
        dateTime = timestamp,
        minDateTime = null,
        isTimeSet = true,
        dateSelectionEnabled = dateSelectionEnabled,
        locationText = restaurantName,
        searchResults = searchResults,
    )
    override val endState: ManualAddPlanState = ManualAddPlanState(
        dateTime = null,
        minDateTime = null,
        isTimeSet = false,
        dateSelectionEnabled = dateSelectionEnabled,
        locationText = null,
        searchResults = emptyList(),
    )
}

data class AddFlexibleSectionItemState(
    override val id: String,
    override val typeSelectionEnabled: Boolean,
    override val dateSelectionEnabled: Boolean,
    override val saveButtonEnabled: Boolean,
    override val deleteButtonEnabled: Boolean,
    val startDateTime: ZonedDateTime,
    val hasStartTime: Boolean,
    val sectionName: String?,
) : AddPlanItemState {
    override val timestamp: ZonedDateTime = startDateTime
}

data class SearchResultItemState(val title: String, val subtitle: String)

val AddPlanItemState.type
    get() = when (this) {
        is AddFlightItemState -> AddPlanItemState.Type.Flight
        is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanItemState.Type.Lodging

        is AddPlaceItemState -> AddPlanItemState.Type.Place
        is AddRestaurantItemState -> AddPlanItemState.Type.Restaurant
        is AddFlexibleSectionItemState -> AddPlanItemState.Type.FlexibleSection
    }
