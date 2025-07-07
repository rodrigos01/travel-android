package travel.vola.android.model.room

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripRepository
import java.util.UUID

class RoomTripRepository(private val dao: TripDao) : TripRepository {
    override val trips: Flow<List<Trip>>
        get() = dao.observeTrips().map { it.map { trip -> trip.toAppDataModel() } }

    override fun findTripById(tripId: String): Flow<Trip> {
        return dao.observeTrip(tripId).map { it.toAppDataModel() }
    }

    override fun getTripFlights(tripId: String): Flow<List<Flight>> {
        return findTripById(tripId).map { trip -> trip.flights }
    }

    override fun getTripHotels(tripId: String): Flow<List<Lodging>> {
        return findTripById(tripId).map { trip -> trip.lodgings }
    }

    override suspend fun addTrip(): String {
        val id = UUID.randomUUID().toString()
        dao.addTrip(RoomData.Schema.Trip(id, null, null))
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

    override suspend fun saveFlight(tripId: String, flight: Flight) {
        dao.saveFlight(
            RoomData.Schema.Flight(
                id = flight.id,
                tripId = tripId,
                price = flight.price,
            )
        )
        flight.segments.forEachIndexed { index, segment ->
            savePlace(segment.airportFrom.city)
            dao.saveAirport(
                RoomData.Schema.Airport(
                    iata = segment.airportFrom.iata,
                    name = segment.airportFrom.name,
                    timeZone = segment.airportFrom.timeZone,
                    city = segment.airportFrom.city.id,
                )
            )
            savePlace(segment.airportTo.city)
            dao.saveAirport(
                RoomData.Schema.Airport(
                    iata = segment.airportTo.iata,
                    name = segment.airportTo.name,
                    timeZone = segment.airportTo.timeZone,
                    city = segment.airportTo.city.id,
                )
            )
            dao.saveFlightSegment(
                RoomData.Schema.FlightSegment(
                    id = "${flight.id}_$index",
                    flightId = flight.id,
                    airportFrom = segment.airportFrom.iata,
                    airportTo = segment.airportTo.iata,
                    departure = segment.departure,
                    arrival = segment.arrival,
                )
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
            )
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
            )
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

    private suspend fun withTrip(tripId: String, block: suspend (RoomData.Schema.Trip) -> Unit) =
        block(dao.getTrip(tripId))

    private suspend fun savePlace(place: Place) {
        dao.savePlace(
            RoomData.Place(
                id = place.id,
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude,
                coverImage = place.coverImage,
                address = place.address,
                externalId = place.externalId,
                source = place.source,
            )
        )
    }
}