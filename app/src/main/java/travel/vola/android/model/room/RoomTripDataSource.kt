package travel.vola.android.model.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import travel.vola.android.model.data.DataSourceType
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripPreferences
import travel.vola.android.model.repository.TripDataSource
import java.util.UUID

class RoomTripDataSource(private val dao: TripDao) : TripDataSource {

    override val dataSourceType: DataSourceType = DataSourceType.LOCAL

    override val trips: Flow<List<Trip>>
        get() = dao.observeTrips().map { it.map { trip -> trip.toAppDataModel() } }

    override fun findTripById(tripId: String): Flow<Trip?> {
        return dao.observeTrip(tripId).map { it?.toAppDataModel() }
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId).map { trip -> trip?.flights ?: emptyList() }
    }

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> {
        return findTripById(tripId).map { trip -> trip?.lodgings ?: emptyList() }
    }

    override suspend fun addTrip(): String {
        val id = UUID.randomUUID().toString()
        dao.addTrip(RoomData.Schema.Trip(id, null, null, null))
        return id
    }

    override suspend fun addTrip(
        name: String,
        places: List<TimedPlace>,
        preferences: TripPreferences,
    ): String {
        val id = UUID.randomUUID().toString()
        dao.addTrip(
            RoomData.Schema.Trip(
                id,
                name,
                coverImage = null,
                preferences = preferences.toRoomDataModel(),
            ),
        )
        places.forEach {
            saveTimedPlace(id, it)
        }
        return id
    }

    override suspend fun deleteTrip(tripId: String) {
        withTrip(tripId) { trip ->
            dao.deleteTrip(trip)
        }
    }

    override suspend fun updateName(tripId: String, newName: String) {
        withTrip(tripId) { trip ->
            dao.updateTrip(trip.copy(name = newName))
        }
    }

    override suspend fun updateTripPreferences(
        tripId: String,
        preferences: TripPreferences,
    ) {
        withTrip(tripId) { trip ->
            dao.updateTrip(trip.copy(preferences = preferences.toRoomDataModel()))
        }
    }

    override suspend fun saveFlight(tripId: String, flight: Flight) {
        dao.saveFlight(
            RoomData.Schema.Flight(
                id = flight.id,
                tripId = tripId,
                price = flight.price,
            ),
        )
        flight.segments.forEachIndexed { index, segment ->
            savePlace(segment.airportFrom.city)
            dao.saveAirport(
                RoomData.Schema.Airport(
                    iata = segment.airportFrom.iata,
                    name = segment.airportFrom.name,
                    timeZone = segment.airportFrom.timeZone,
                    city = segment.airportFrom.city.id,
                ),
            )
            savePlace(segment.airportTo.city)
            dao.saveAirport(
                RoomData.Schema.Airport(
                    iata = segment.airportTo.iata,
                    name = segment.airportTo.name,
                    timeZone = segment.airportTo.timeZone,
                    city = segment.airportTo.city.id,
                ),
            )
            dao.saveFlightSegment(
                RoomData.Schema.FlightSegment(
                    id = "${flight.id}_$index",
                    flightId = flight.id,
                    airportFrom = segment.airportFrom.iata,
                    airportTo = segment.airportTo.iata,
                    departure = segment.departure,
                    arrival = segment.arrival,
                ),
            )
        }
    }

    override suspend fun saveLodging(tripId: String, lodging: Lodging) {
        savePlace(lodging.city)
        dao.saveLodging(
            RoomData.Schema.Lodging(
                id = lodging.id,
                tripId = tripId,
                name = lodging.name,
                address = lodging.address,
                latitude = lodging.latitude,
                longitude = lodging.longitude,
                city = lodging.city.id,
                checkIn = lodging.checkIn,
                checkout = lodging.checkout,
            ),
        )
    }

    override suspend fun saveTimedPlace(tripId: String, timedPlace: TimedPlace) {
        savePlace(timedPlace.place)
        savePlace(timedPlace.city)
        dao.saveTimedPlace(
            RoomData.Schema.TimedPlace(
                id = timedPlace.id,
                tripId = tripId,
                startDateTime = timedPlace.startDateTime,
                hasStartTime = timedPlace.hasStartTime,
                endDateTime = timedPlace.endDateTime,
                hasEndTime = timedPlace.hasEndTime,
                place = timedPlace.place.id,
                city = timedPlace.city.id,
            ),
        )
    }

    override suspend fun saveRestaurantReservation(
        tripId: String,
        restaurantReservation: RestaurantReservation,
    ) {
        savePlace(restaurantReservation.place)
        savePlace(restaurantReservation.city)
        dao.saveRestaurantReservation(
            RoomData.Schema.RestaurantReservation(
                id = restaurantReservation.id,
                tripId = tripId,
                dateTime = restaurantReservation.dateTime,
                place = restaurantReservation.place.id,
                city = restaurantReservation.city.id,
            ),
        )
    }

    override suspend fun saveFlexibleSection(
        tripId: String,
        flexibleSection: FlexibleDaySection,
    ) {
        flexibleSection.categories.forEach { category ->
            val categoryId = UUID.randomUUID().toString()
            category.items.forEach {
                savePlace(it.place)
                dao.saveFlexibleSectionItem(
                    RoomData.Schema.FlexibleSectionItem(
                        id = it.id,
                        categoryId = categoryId,
                        place = it.place.id,
                        note = it.note,
                    ),
                )
            }
            dao.saveFlexibleSectionCategory(
                RoomData.Schema.FlexibleSectionCategory(
                    id = categoryId,
                    sectionId = flexibleSection.id,
                    name = category.name,
                ),
            )
        }
        dao.saveFlexibleSection(
            RoomData.Schema.FlexibleSection(
                id = flexibleSection.id,
                tripId = tripId,
                name = flexibleSection.name,
                date = flexibleSection.date,
                city = flexibleSection.city.id,
            ),
        )
    }

    override suspend fun deleteFlight(tripId: String, flightId: String) {
        dao.getFlight(tripId, flightId).let { flight ->
            dao.deleteFlight(flight)
        }
    }

    override suspend fun deleteLodging(tripId: String, lodgingId: String) {
        dao.getLodging(tripId, lodgingId).let { lodging ->
            dao.deleteLodging(lodging)
        }
    }

    override suspend fun deleteTimedPlace(tripId: String, timedPlaceId: String) {
        dao.getTimedPlace(tripId, timedPlaceId).let { timedPlace ->
            dao.deleteTimedPlace(timedPlace)
        }
    }

    override suspend fun deleteRestaurantReservation(
        tripId: String,
        restaurantReservationId: String,
    ) {
        dao.getRestaurantReservation(tripId, restaurantReservationId).let { restaurantReservation ->
            dao.deleteRestaurantReservation(restaurantReservation)
        }
    }

    override suspend fun deleteFlexibleSection(
        tripId: String,
        flexibleSectionId: String,
    ) {
        val section = dao.getFlexibleSection(tripId, flexibleSectionId)
        dao.deleteFlexibleSection(section)
    }

    private suspend fun withTrip(tripId: String, block: suspend (RoomData.Schema.Trip) -> Unit) =
        block(dao.getTrip(tripId))

    private suspend fun savePlace(place: Place) {
        dao.savePlace(
            RoomData.Place(
                id = place.id,
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude,
                timeZone = place.timeZone,
                coverImage = place.coverImage,
                address = place.address,
                externalId = place.externalId,
                source = place.source,
            ),
        )
    }
}
