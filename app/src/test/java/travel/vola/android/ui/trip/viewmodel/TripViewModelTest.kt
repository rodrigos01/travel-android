package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.remove
import travel.vola.android.extensions.set
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Time
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.DateRangeItemState
import travel.vola.android.ui.trip.state.TripItemState.EmptyDateItemState
import travel.vola.android.ui.trip.state.TripItemState.FlightArrivalItemState
import travel.vola.android.ui.trip.state.TripItemState.FlightDepartureItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckInItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckOutItemState
import travel.vola.android.ui.trip.state.TripItemState.MonthItemState
import travel.vola.android.ui.trip.state.TripItemState.PlaceItemState
import java.util.TimeZone

class TripViewModelTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val tripFlow = MutableStateFlow<Trip>(mock())
    private val repository = mock<TripRepository> {
        on { findTripById("tripId") } doReturn tripFlow
    }

    private val addPlanItems = MutableStateFlow<Map<String, AddPlanItemState>>(mapOf())
    private val addPlanUseCase: AddPlanUseCase = mock {
        on { items } doReturn addPlanItems
    }
    private val subject =
        TripViewModel(
            repository,
            mock(),
            "tripId",
            mock(),
            mock(),
            addPlanUseCase,
        )

    private fun String?.asTime(): Time = this?.let { Time(this) } ?: Time(0L, TimeZone.getDefault())

    @Test
    fun `events should have one departure event per flight`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    airportFromName = "John F. Kennedy Intl. Airport",
                    departure = "2024-05-10T22:05:00-04:00",
                ),
                Flight(
                    id = "opo-par",
                    airportFromName = "Francisco Sá Carneiro Airport",
                    departure = "2024-05-21T16:50:00+01:00",
                ),
                Flight(
                    id = "lis-jfk",
                    airportFromName = "Humberto Delgado International Airport",
                    departure = "2024-06-14T17:05:00+01:00",
                ),
            )
        )
        val departures =
            subject.viewState.value.items.filterIsInstance(FlightDepartureItemState::class.java)
        assertThat(departures).satisfiesExactly(
            { item ->
                assertThat(item.airport).isEqualTo("John F. Kennedy Intl. Airport")
                assertThat(item.dayOfMonth).isEqualTo("10")
                assertThat(item.time).isEqualTo("10:05 PM")
            },
            { item ->
                assertThat(item.airport).isEqualTo("Francisco Sá Carneiro Airport")
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("4:50 PM")
            },
            { item ->
                assertThat(item.airport).isEqualTo("Humberto Delgado International Airport")
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("5:05 PM")
            },
        )
    }

    @Test
    fun `events should have one arrival event per flight`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                ),
                Flight(
                    id = "opo-par",
                    departure = "2024-05-21T16:50:00+01:00",
                    airportToName = "Orly International Airport",
                    arrival = "2024-05-21T20:15:00+02:00",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                ),
            )
        )
        val arrivals =
            subject.viewState.value.items.filterIsInstance(FlightArrivalItemState::class.java)
        assertThat(arrivals).satisfiesExactly({ item ->
            assertThat(item.dayOfMonth).isEqualTo("11")
            assertThat(item.time).isEqualTo("10:00 AM")
        }, { item ->
            assertThat(item.dayOfMonth).isEqualTo("21")
            assertThat(item.time).isEqualTo("8:15 PM")
        }, { item ->
            assertThat(item.dayOfMonth).isEqualTo("14")
            assertThat(item.time).isEqualTo("8:05 PM")
        })
    }

    @Test
    fun `events should have one check-in event per hotel`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00:00+01:00",
                    checkout = "2024-05-21T11:00:00+01:00",
                ), Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-06-02T11:00:00+02:00",
                ), Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00:00+02:00",
                    checkout = "2024-06-14T11:00:00+02:00",
                )
            )
        )
        val lodgings = subject.viewState.value.items.filterIsInstance<HotelCheckInItemState>()
        assertThat(lodgings).satisfiesExactly({ item ->
            assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
            assertThat(item.hotelAddress).isEqualTo("R. de Sá da Bandeira 91, 4000-427 Porto, Portugal")
            assertThat(item.dayOfMonth).isEqualTo("19")
            assertThat(item.time).isEqualTo("1:00 PM")
        }, { item ->
            assertThat(item.hotelName).isEqualTo("Hôtel La Villa Nice Victor Hugo")
            assertThat(item.hotelAddress).isEqualTo("19 Bis Bd Victor Hugo, 06000 Nice, France")
            assertThat(item.dayOfMonth).isEqualTo("29")
            assertThat(item.time).isEqualTo("1:00 PM")
        }, { item ->
            assertThat(item.hotelName).isEqualTo("Hotel Conca Park")
            assertThat(item.hotelAddress).isEqualTo("Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy")
            assertThat(item.dayOfMonth).isEqualTo("12")
            assertThat(item.time).isEqualTo("1:00 PM")
        })
    }

    @Test
    fun `events should have one checkout event per hotel`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00:00+01:00",
                    checkout = "2024-05-21T11:00:00+01:00",
                ), Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-06-02T11:00:00+02:00",
                ), Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00:00+02:00",
                    checkout = "2024-06-14T11:00:00+02:00",
                )
            )
        )
        val lodgings = subject.viewState.value.items.filterIsInstance<HotelCheckOutItemState>()
        assertThat(lodgings).satisfiesExactly({ item ->
            assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
            assertThat(item.dayOfMonth).isEqualTo("21")
            assertThat(item.time).isEqualTo("11:00 AM")
        }, { item ->
            assertThat(item.hotelName).isEqualTo("Hôtel La Villa Nice Victor Hugo")
            assertThat(item.dayOfMonth).isEqualTo("2")
            assertThat(item.time).isEqualTo("11:00 AM")
        }, { item ->
            assertThat(item.hotelName).isEqualTo("Hotel Conca Park")
            assertThat(item.dayOfMonth).isEqualTo("14")
            assertThat(item.time).isEqualTo("11:00 AM")
        })
    }

    @Test
    fun `events should have one place event for each place`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "opo-par",
                    departure = "2024-05-21T16:50:00+01:00",
                    airportToName = "Orly International Airport",
                    arrival = "2024-05-21T20:15:00+02:00",
                    cityFromName = "Porto",
                    cityToName = "Nice",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ), lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00:00+01:00",
                    checkout = "2024-05-21T11:00:00+01:00",
                    cityName = "Porto"
                ), Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-06-02T11:00:00+02:00",
                    cityName = "Nice"
                ), Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00:00+02:00",
                    checkout = "2024-06-14T11:00:00+02:00",
                    cityName = "Sorrento"
                )
            )
        )
        val places = subject.viewState.value.items.filterIsInstance<PlaceItemState>()
        assertThat(places).satisfiesExactly({ item ->
            assertThat(item.placeName).isEqualTo("Porto")
            assertThat(item.dateStart).isEqualTo("May 11")
            assertThat(item.dateEnd).isEqualTo("May 21")
        }, { item ->
            assertThat(item.placeName).isEqualTo("Nice")
            assertThat(item.dateStart).isEqualTo("May 21")
            assertThat(item.dateEnd).isEqualTo("Jun 2")
        }, { item ->
            assertThat(item.placeName).isEqualTo("Sorrento")
            assertThat(item.dateStart).isEqualTo("Jun 12")
            assertThat(item.dateEnd).isEqualTo("Jun 14")
        })
    }

    @Test
    fun `events should not have place item for origin`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ), lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00:00+01:00",
                    checkout = "2024-05-21T11:00:00+01:00",
                    cityName = "Lisbon"
                ),
            )
        )
        val places = subject.viewState.value.items.filterIsInstance<PlaceItemState>()
        assertThat(places).noneSatisfy {
            assertThat(it.placeName).isEqualTo("New York")
        }
    }

    @Test
    fun `events should not have place item for places with only departure event`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                    cityFromName = "Lisbon",
                    cityToName = "New York",
                ),
            ), lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00:00+01:00",
                    checkout = "2024-05-21T11:00:00+01:00",
                    cityName = "Porto"
                ),
            )
        )
        val places = subject.viewState.value.items.filterIsInstance<PlaceItemState>()
        assertThat(places).noneSatisfy {
            assertThat(it.placeName).isEqualTo("Lisbon")
        }
    }

    @Test
    fun `events should have one month event for each month`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ),
        )
        val months = subject.viewState.value.items.filterIsInstance<MonthItemState>()
        assertThat(months).satisfiesExactly({ item ->
            assertThat(item.month).isEqualTo("May")
            assertThat(item.year).isEqualTo("2024")
        }, { item ->
            assertThat(item.month).isEqualTo("June")
            assertThat(item.year).isEqualTo("2024")
        })
    }

    @Test
    fun `first events should be first of each day`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportFromName = "John F. Kennedy Intl. Airport",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:25:00+01:00",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportFromName = "Humberto Delgado International Airport",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:15:00-04:00",
                ),
            ), lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-06-14T11:00:00+01:00",
                ),
            )
        )
        val eventItems =
            subject.viewState.value.items.filterIsInstance<TripItemState.EventItemState>()
        assertThat(eventItems).satisfiesExactly(
            {
                val item = it as FlightDepartureItemState
                assertThat(item.airport).isEqualTo("John F. Kennedy Intl. Airport")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as FlightArrivalItemState
                assertThat(item.airport).isEqualTo("Humberto Delgado International Airport")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as HotelCheckInItemState
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.showDate).isFalse
            },
            {
                val item = it as HotelCheckOutItemState
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as FlightDepartureItemState
                assertThat(item.airport).isEqualTo("Humberto Delgado International Airport")
                assertThat(item.showDate).isFalse
            },
            {
                val item = it as FlightArrivalItemState
                assertThat(item.airport).isEqualTo("John F. Kennedy Intl. Airport")
                assertThat(item.showDate).isFalse
            },
        )
    }

    @Test
    fun `events should have empty date range for all dates without events`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val dateRanges = subject.viewState.value.items.filterIsInstance<DateRangeItemState>()
        assertThat(dateRanges).satisfiesExactly({ item ->
            assertThat(item.dayOfMonthStart).isEqualTo("12")
            assertThat(item.dayOfMonthEnd).isEqualTo("18")
        })
    }

    @Test
    fun `empty date range should not have start before end`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Porto",
                )
            )
        )
        val dateRanges = subject.viewState.value.items.filterIsInstance<DateRangeItemState>()
        assertThat(dateRanges).noneSatisfy { item ->
            assertThat(item.dayOfMonthStart).isEqualTo("11")
            assertThat(item.dayOfMonthEnd).isEqualTo("10")
        }
    }

    @Test
    fun `empty date range must have at least 2 days`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-13T11:00:00+01:00",
                ),
            )
        )
        val dateRanges = subject.viewState.value.items.filterIsInstance<DateRangeItemState>()
        assertThat(dateRanges).noneSatisfy { item ->
            assertThat(item.dayOfMonthStart).isEqualTo("12")
            assertThat(item.dayOfMonthEnd).isEqualTo("12")
        }
    }

    @Test
    fun `events should have single empty date item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-13T11:00:00+01:00",
                ),
            )
        )
        val dateRanges = subject.viewState.value.items.filterIsInstance<EmptyDateItemState>()
        assertThat(dateRanges).satisfiesExactly({ item ->
            assertThat(item.dayOfMonth).isEqualTo("12")
        })
    }

    @Test
    fun `last item on day should have empty add item after it`() {
        val lodgingName = "Best Western Premier Hotel Montfleuri"
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = lodgingName,
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-05-30T11:00:00+02:00",
                ),
            )
        )
        val checkInItemIndex =
            subject.viewState.value.items.indexOfFirst { it is HotelCheckInItemState && it.hotelName == lodgingName }
        val addPlanItem = subject.viewState.value.items[checkInItemIndex + 1]
        assertThat(addPlanItem).isInstanceOf(TripItemState.EmptyAddPlanItemState::class.java)
    }

    @Test
    fun `last item in place should have empty add item after it`() {
        val lodgingName = "Best Western Premier Hotel Montfleuri"
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = lodgingName,
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-05-30T11:00:00+02:00",
                    cityName = "Montfleuri"
                ),
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    checkIn = "2024-05-30T13:00:00+02:00",
                    checkout = "2024-06-01T11:00:00+02:00",
                    cityName = "Nice"
                ),
            )
        )
        val checkOutItemIndex =
            subject.viewState.value.items.indexOfFirst { it is HotelCheckOutItemState && it.hotelName == lodgingName }
        val addPlanItem = subject.viewState.value.items[checkOutItemIndex + 1]
        assertThat(addPlanItem).isInstanceOf(TripItemState.EmptyAddPlanItemState::class.java)
    }

    @Test
    fun `last item in list should be empty add item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    checkIn = "2024-05-30T13:00:00+02:00",
                    checkout = "2024-06-01T11:00:00+02:00",
                    cityName = "Nice"
                ),
            )
        )
        val addPlanItem = subject.viewState.value.items.last()
        assertThat(addPlanItem).isInstanceOf(TripItemState.EmptyAddPlanItemState::class.java)
    }

    @Test
    fun `last item before single departure event should not have empty add item after it`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05:00-04:00",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00:00+01:00",
                    cityFromName = "New York",
                    cityToName = "Lisbon",
                ),
                Flight(
                    id = "por-par",
                    departure = "2024-05-21T17:05:00+01:00",
                    airportToName = "Orly Airport",
                    arrival = "2024-05-21T19:25:00+02:00",
                    cityFromName = "Porto",
                    cityToName = "Paris",
                ),
                Flight(
                    id = "par-jfk",
                    departure = "2024-06-14T17:05:00+01:00",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05:00-04:00",
                    cityFromName = "Porto",
                    cityToName = "New York",
                ),
            ), lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-21T13:00:00+01:00",
                    checkout = "2024-06-14T11:00:00+01:00",
                    cityName = "Paris"
                ),
            )
        )
        val departureItemIndex = subject.viewState.value.items.indexOfFirst {
            it is FlightDepartureItemState && it.destination == "New York"
        }
        val itemBefore = subject.viewState.value.items[departureItemIndex - 1]
        assertThat(itemBefore).isNotInstanceOf(TripItemState.EmptyAddPlanItemState::class.java)
    }

    @Test
    fun `empty add plan item should not appear before empty date range`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val dateRangeItemIndex =
            subject.viewState.value.items.indexOfFirst { it is DateRangeItemState }
        val itemBefore = subject.viewState.value.items[dateRangeItemIndex - 1]
        assertThat(itemBefore).isNotInstanceOf(TripItemState.EmptyAddPlanItemState::class.java)
    }

    @Test
    fun `add Plan tapped should add add plan item at tapped item index`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val expected: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.first { it is TripItemState.EmptyAddPlanItemState } as TripItemState.EmptyAddPlanItemState
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.addButtonTapped(originalItem.id)
        val addedItem = subject.viewState.value.items[originalItemIndex]
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `add plan tapped on last item in place should add add plan item with last item time and start date selection disabled`() {
        val lodgingName = "Best Western Premier Hotel Montfleuri"
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = lodgingName,
                    checkIn = "2024-05-29T13:00:00+02:00",
                    checkout = "2024-05-30T11:00:00+02:00",
                    cityName = "Montfleuri"
                ),
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    checkIn = "2024-05-30T13:00:00+02:00",
                    checkout = "2024-06-01T11:00:00+02:00",
                    cityName = "Nice"
                ),
            )
        )
        val checkOutItem =
            subject.viewState.value.items.first { it is HotelCheckOutItemState && it.hotelName == lodgingName } as TripItemState.EventItemState
        subject.addButtonTapped(checkOutItem.id)
        verify(addPlanUseCase).createAddPlanItem(
            any(),
            eq(Time("2024-05-30T11:00:00+02:00")),
            dateSelectionEnabled = eq(false),
            type = any(),
        )
    }

    @Test
    fun `add Plan tapped on date range should add add plan item below tapped item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val expected: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.addButtonTapped(originalItem.id)
        val addedItem = subject.viewState.value.items[originalItemIndex]
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `add Plan tapped on date range should add add plan item with start date and start date selection enabled`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        verify(addPlanUseCase).createAddPlanItem(
            id = eq(originalItem.id),
            time = any(), dateSelectionEnabled = eq(true), type = any(),
        )
    }

    @Test
    fun `empty date row tapped on date range should replace tapped item with add plan item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-13T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val expected: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<EmptyDateItemState>().first()
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.emptyDateRowTapped(originalItem.id)
        val addedItem = subject.viewState.value.items[originalItemIndex]
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `save should add new flight to repository`() = runTest {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        val entity: Flight = mock()
        mockAddPlanItem(addPlanItem)
        addPlanUseCase.stub {
            on { saveItem(addPlanItemId) } doReturn entity
        }
        val originalItem =
            subject.viewState.value.items.first { it is TripItemState.EmptyAddPlanItemState } as TripItemState.EmptyAddPlanItemState
        subject.addButtonTapped(originalItem.id)
        subject.save(addPlanItemId)
        verify(addPlanUseCase).saveItem(addPlanItemId)
        verify(repository).saveFlight("tripId", entity)
    }

    @Test
    fun `save should add new lodging to repository`() = runTest {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        val entity: Lodging = mock()
        mockAddPlanItem(addPlanItem)
        addPlanUseCase.stub {
            on { saveItem(addPlanItemId) } doReturn entity
        }
        val originalItem =
            subject.viewState.value.items.first { it is TripItemState.EmptyAddPlanItemState } as TripItemState.EmptyAddPlanItemState
        subject.addButtonTapped(originalItem.id)
        subject.save(addPlanItemId)
        verify(addPlanUseCase).saveItem(addPlanItemId)
        verify(repository).saveLodging("tripId", entity)
    }

    @Test
    fun `addPlanUseCase items changed should update existing item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(addPlanItem)
        val originalItem =
            subject.viewState.value.items.first { it is TripItemState.EmptyAddPlanItemState } as TripItemState.EmptyAddPlanItemState
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.addButtonTapped(originalItem.id)
        val newAddPlanItem = mock<AddFlightItemState> {
            on { id } doReturn addPlanItemId
        }
        addPlanItems[originalItem.id] = newAddPlanItem
        val resultAddPlanItem = subject.viewState.value.items[originalItemIndex]
        assertThat(resultAddPlanItem).isEqualTo(newAddPlanItem)
    }

    @Test
    fun `addPlanUseCase items changed invalid item should ignore`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val newItemId = "originalItemId"
        val newAddPlanItem = mock<AddFlightItemState> {
            on { id } doReturn newItemId
        }
        addPlanItems.value = mapOf(newItemId to newAddPlanItem)
        assertThat(subject.viewState.value.items).doesNotContain(newAddPlanItem)
    }

    @Test
    fun `cancel should remove item from addPlanUseCase`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val expected: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        subject.cancelEdit(addPlanItemId)
        verify(addPlanUseCase).removeItem(addPlanItemId)
    }

    @Test
    fun `cancel should remove item from use case`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        subject.cancelEdit(addPlanItemId)
        verify(addPlanUseCase).removeItem(addPlanItemId)
    }

    @Test
    fun `item removed from usecase should reinsert replaceable item`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00:00+01:00",
                    checkout = "2024-05-19T11:00:00+01:00",
                ),
            )
        )
        val addPlanItemId = "originalItemId"
        val addPlanItemTimestamp: Time = mock()
        val addPlanItem: AddPlanItemState = mock {
            on { id } doReturn addPlanItemId
            on { timestamp } doReturn addPlanItemTimestamp
        }
        mockAddPlanItem(addPlanItem)
        val originalItem =
            subject.viewState.value.items.first { it is TripItemState.EmptyAddPlanItemState } as TripItemState.EmptyAddPlanItemState
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.addButtonTapped(originalItem.id)
        subject.cancelEdit(addPlanItemId)
        addPlanItems.remove(originalItem.id)
        val resultAddPlanItem = subject.viewState.value.items[originalItemIndex]
        assertThat(resultAddPlanItem).isEqualTo(originalItem)
    }

    private fun mockAddPlanItem(addPlanItem: AddPlanItemState) {
        addPlanUseCase.stub {
            on { createAddPlanItem(any(), any(), any(), any()) } doAnswer {
                val id = it.getArgument<String>(0)
                addPlanItems.value = mapOf(id to addPlanItem)
            }
        }
    }

    private fun Trip(
        id: String = "tripId",
        flights: List<Flight> = emptyList(),
        lodgings: List<Lodging> = emptyList(),
        places: List<TimedPlace> = emptyList(),
    ) = Trip(
        id = id,
        name = null,
        coverImage = null,
        flights = flights,
        lodgings = lodgings,
        places = places,
    )

    private fun Flight(
        id: String = "flightId",
        departure: String? = null,
        airportFromIata: String = "",
        airportFromName: String = "",
        cityFromName: String = "",
        arrival: String? = null,
        airportToIata: String = "",
        airportToName: String = "",
        cityToName: String = "",
    ) = Flight(
        id = id,
        segments = listOf(
            FlightSegment(
                airportFrom = Airport(
                    iata = airportFromIata,
                    name = airportFromName,
                    timeZone = TimeZone.getDefault(),
                    city = Place(cityFromName),
                ),
                departure = departure.asTime(),
                airportTo = Airport(
                    iata = airportToIata,
                    name = airportToName,
                    timeZone = TimeZone.getDefault(),
                    city = Place(cityToName),
                ),
                arrival = arrival.asTime(),
            )
        ),
    )

    private fun Lodging(
        id: String = "",
        name: String? = null,
        address: String = "",
        checkIn: String? = null,
        checkout: String? = null,
        cityName: String = ""
    ) = Lodging(
        id = id,
        name = name,
        address = address,
        checkIn = checkIn.asTime(),
        checkout = checkout.asTime(),
        city = Place(cityName),
    )

    private fun Place(name: String) = Place(
        id = name,
        name = name,
        address = "",
        latitude = 0.0,
        longitude = 0.0,
        coverImage = null,
        externalId = "",
        source = "",
    )
}
