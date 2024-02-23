@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui

import com.combah.travel2.extensions.Time
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.viewmodel.TripViewModel
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.DateRangeItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.FlightArrivalItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.FlightDepartureItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.HotelCheckInItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.HotelCheckOutItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.MonthItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.PlaceItem
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.TimeZone
import kotlin.contracts.ExperimentalContracts

class TripViewModelTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val tripFlow = MutableStateFlow<Trip>(mock())
    private val repository = mock<TripRepository> {
        on { findTripById("tripId") } doReturn tripFlow
    }
    private val subject = TripViewModel(repository, "tripId")

    private fun String?.asTime(): Time =
        this?.let { Time(this) } ?: Time(0L, TimeZone.getDefault())

    @Test
    fun `events should have one departure event per flight`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    airportFromName = "John F. Kennedy Intl. Airport",
                    departure = "2024-05-10T22:05 -0400",
                ),
                Flight(
                    id = "opo-par",
                    airportFromName = "Francisco Sá Carneiro Airport",
                    departure = "2024-05-21T16:50 +0100",
                ),
                Flight(
                    id = "lis-jfk",
                    airportFromName = "Humberto Delgado International Airport",
                    departure = "2024-06-14T17:05 +0100",
                ),
            )
        )
        val departures =
            subject.viewState.value.items.filterIsInstance(FlightDepartureItem::class.java)
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
                    departure = "2024-05-10T22:05 -0400",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00 +0100",
                ),
                Flight(
                    id = "opo-par",
                    departure = "2024-05-21T16:50 +0100",
                    airportToName = "Orly International Airport",
                    arrival = "2024-05-21T20:15 +0200",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05 +0100",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05 -0400",
                ),
            )
        )
        val arrivals =
            subject.viewState.value.items.filterIsInstance(FlightArrivalItem::class.java)
        assertThat(arrivals).satisfiesExactly(
            { item ->
                assertThat(item.dayOfMonth).isEqualTo("11")
                assertThat(item.time).isEqualTo("10:00 AM")
            },
            { item ->
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("8:15 PM")
            },
            { item ->
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("8:05 PM")
            }
        )
    }

    @Test
    fun `events should have one check-in event per hotel`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00 +0100",
                    checkout = "2024-05-21T11:00 +0100",
                ),
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00 +0200",
                    checkout = "2024-06-02T11:00 +0200",
                ),
                Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00 +0200",
                    checkout = "2024-06-14T11:00 +0200",
                )
            )
        )
        val lodgings =
            subject.viewState.value.items.filterIsInstance<HotelCheckInItem>()
        assertThat(lodgings).satisfiesExactly(
            { item ->
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.hotelAddress).isEqualTo("R. de Sá da Bandeira 91, 4000-427 Porto, Portugal")
                assertThat(item.dayOfMonth).isEqualTo("19")
                assertThat(item.time).isEqualTo("1:00 PM")
            },
            { item ->
                assertThat(item.hotelName).isEqualTo("Hôtel La Villa Nice Victor Hugo")
                assertThat(item.hotelAddress).isEqualTo("19 Bis Bd Victor Hugo, 06000 Nice, France")
                assertThat(item.dayOfMonth).isEqualTo("29")
                assertThat(item.time).isEqualTo("1:00 PM")
            },
            { item ->
                assertThat(item.hotelName).isEqualTo("Hotel Conca Park")
                assertThat(item.hotelAddress).isEqualTo("Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy")
                assertThat(item.dayOfMonth).isEqualTo("12")
                assertThat(item.time).isEqualTo("1:00 PM")
            }
        )
    }

    @Test
    fun `events should have one checkout event per hotel`() {
        tripFlow.value = Trip(
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00 +0100",
                    checkout = "2024-05-21T11:00 +0100",
                ),
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00 +0200",
                    checkout = "2024-06-02T11:00 +0200",
                ),
                Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00 +0200",
                    checkout = "2024-06-14T11:00 +0200",
                )
            )
        )
        val lodgings =
            subject.viewState.value.items.filterIsInstance<HotelCheckOutItem>()
        assertThat(lodgings).satisfiesExactly(
            { item ->
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("11:00 AM")
            },
            { item ->
                assertThat(item.hotelName).isEqualTo("Hôtel La Villa Nice Victor Hugo")
                assertThat(item.dayOfMonth).isEqualTo("2")
                assertThat(item.time).isEqualTo("11:00 AM")
            },
            { item ->
                assertThat(item.hotelName).isEqualTo("Hotel Conca Park")
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("11:00 AM")
            }
        )
    }

    @Test
    fun `events should have one place event for each place`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05 -0400",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00 +0100",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "opo-par",
                    departure = "2024-05-21T16:50 +0100",
                    airportToName = "Orly International Airport",
                    arrival = "2024-05-21T20:15 +0200",
                    cityFromName = "Porto",
                    cityToName = "Nice",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05 +0100",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05 -0400",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ),
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00 +0100",
                    checkout = "2024-05-21T11:00 +0100",
                    cityName = "Porto"
                ),
                Lodging(
                    name = "Hôtel La Villa Nice Victor Hugo",
                    address = "19 Bis Bd Victor Hugo, 06000 Nice, France",
                    checkIn = "2024-05-29T13:00 +0200",
                    checkout = "2024-06-02T11:00 +0200",
                    cityName = "Nice"
                ),
                Lodging(
                    name = "Hotel Conca Park",
                    address = "Via degli Aranci, 13\\bis, 80067 Sorrento NA, Italy",
                    checkIn = "2024-06-12T13:00 +0200",
                    checkout = "2024-06-14T11:00 +0200",
                    cityName = "Sorrento"
                )
            )
        )
        val places = subject.viewState.value.items.filterIsInstance<PlaceItem>()
        assertThat(places).satisfiesExactly(
            { item ->
                assertThat(item.placeName).isEqualTo("Porto")
                assertThat(item.dateStart).isEqualTo("May 11")
                assertThat(item.dateEnd).isEqualTo("May 21")
            },
            { item ->
                assertThat(item.placeName).isEqualTo("Nice")
                assertThat(item.dateStart).isEqualTo("May 21")
                assertThat(item.dateEnd).isEqualTo("Jun 2")
            },
            { item ->
                assertThat(item.placeName).isEqualTo("Sorrento")
                assertThat(item.dateStart).isEqualTo("Jun 12")
                assertThat(item.dateEnd).isEqualTo("Jun 14")
            }
        )
    }

    @Test
    fun `events should not have place item for origin`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05 -0400",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00 +0100",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05 +0100",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05 -0400",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ),
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    address = "R. de Sá da Bandeira 91, 4000-427 Porto, Portugal",
                    checkIn = "2024-05-19T13:00 +0100",
                    checkout = "2024-05-21T11:00 +0100",
                    cityName = "Lisbon"
                ),
            )
        )
        val places = subject.viewState.value.items.filterIsInstance<PlaceItem>()
        assertThat(places).noneSatisfy {
            assertThat(it.placeName).isEqualTo("New York")
        }
    }

    @Test
    fun `events should have one month event for each month`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05 -0400",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:00 +0100",
                    cityFromName = "New York",
                    cityToName = "Porto",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05 +0100",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:05 -0400",
                    cityFromName = "Sorrento",
                    cityToName = "New York",
                ),
            ),
        )
        val months = subject.viewState.value.items.filterIsInstance<MonthItem>()
        assertThat(months).satisfiesExactly(
            { item ->
                assertThat(item.month).isEqualTo("May")
                assertThat(item.year).isEqualTo("2024")
            },
            { item ->
                assertThat(item.month).isEqualTo("June")
                assertThat(item.year).isEqualTo("2024")
            }
        )
    }

    @Test
    fun `first events should be first of each day`() {
        tripFlow.value = Trip(
            flights = listOf(
                Flight(
                    id = "jfk-lis",
                    departure = "2024-05-10T22:05 -0400",
                    airportFromName = "John F. Kennedy Intl. Airport",
                    airportToName = "Humberto Delgado International Airport",
                    arrival = "2024-05-11T10:25 +0100",
                ),
                Flight(
                    id = "lis-jfk",
                    departure = "2024-06-14T17:05 +0100",
                    airportFromName = "Humberto Delgado International Airport",
                    airportToName = "John F. Kennedy Intl. Airport",
                    arrival = "2024-06-14T20:15 -0400",
                ),
            ),
            lodgings = listOf(
                Lodging(
                    name = "Pestana Porto - A Brasileira",
                    checkIn = "2024-05-11T13:00 +0100",
                    checkout = "2024-06-14T11:00 +0100",
                ),
            )
        )
        val eventItems =
            subject.viewState.value.items.filterIsInstance<TripViewModel.TripItem.EventItem>()
        assertThat(eventItems).satisfiesExactly(
            {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo("John F. Kennedy Intl. Airport")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as FlightArrivalItem
                assertThat(item.airport).isEqualTo("Humberto Delgado International Airport")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as HotelCheckInItem
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.showDate).isFalse
            },
            {
                val item = it as HotelCheckOutItem
                assertThat(item.hotelName).isEqualTo("Pestana Porto - A Brasileira")
                assertThat(item.showDate).isTrue
            },
            {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo("Humberto Delgado International Airport")
                assertThat(item.showDate).isFalse
            },
            {
                val item = it as FlightArrivalItem
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
                    checkIn = "2024-05-11T13:00 +0100",
                    checkout = "2024-05-19T11:00 +0100",
                ),
            )
        )
        val dateRanges = subject.viewState.value.items.filterIsInstance<DateRangeItem>()
        assertThat(dateRanges).satisfiesExactly({ item ->
            assertThat(item.dayOfMonthStart).isEqualTo("12")
            assertThat(item.dayOfMonthEnd).isEqualTo("18")
        })
    }


    private fun Trip(
        id: String = "tripId",
        flights: List<Flight> = emptyList(),
        lodgings: List<Lodging> = emptyList(),
        places: List<Place> = emptyList(),
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
                    city = Place(cityFromName),
                ),
                departure = departure.asTime(),
                airportTo = Airport(
                    iata = airportToIata,
                    name = airportToName,
                    city = Place(cityToName),
                ),
                arrival = arrival.asTime(),
            )
        ),
    )

    private fun Lodging(
        name: String? = null,
        address: String = "",
        checkIn: String? = null,
        checkout: String? = null,
        cityName: String = ""
    ) = Lodging(
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
