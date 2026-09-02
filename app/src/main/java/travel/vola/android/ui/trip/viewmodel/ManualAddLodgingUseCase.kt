package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.plus
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.creation.usecase.requireCurrent
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime
import java.util.TimeZone
import kotlin.time.Duration.Companion.days

class ManualAddLodgingUseCase(
    private val pendingDataStore: PendingDataStore,
    private val repository: LodgingSearchRepository = LodgingSearchRepository(),
) : AddPlanUseCase.AddItemUseCase<Lodging, ManualAddLodgingItemState>,
        AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState> {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): ManualAddLodgingItemState = createItem(
        id = id,
        checkIn = time.update(hour = 15, minute = 0),
        checkOut = time.update(hour = 10, minute = 0) + 1.days,
        params = params,
    )

    fun createItem(
        id: String,
        checkIn: ZonedDateTime,
        checkOut: ZonedDateTime?,
        params: AddPlanUseCase.StateParams,
    ): ManualAddLodgingItemState {
        pendingDataStore.set(
            PendingDataStore.Entry.Lodging(entityId = null, selectedPlace = null, city = null),
        )
        val minCheckoutTime = checkIn.toMidnight() + 1.days
        return ManualAddLodgingItemState(
            id = id,
            timestamp = checkIn,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = checkIn,
                minDateTime = null,
                isTimeSet = true,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = checkOut ?: minCheckoutTime.update(hour = 10, minute = 0),
                minDateTime = minCheckoutTime,
                isTimeSet = checkOut != null,
                dateSelectionEnabled = true,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = false,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override fun createItem(
        id: String,
        entity: Lodging,
        params: AddPlanUseCase.StateParams,
    ): ManualAddLodgingItemState {
        // Synthetic Place representing the entity's current lodging, so a fresh search-result
        // selection can be compared against it (by id) the same way as any other resolved entry.
        val place = Place(
            id = entity.id,
            name = entity.name ?: entity.address,
            address = entity.address,
            latitude = entity.latitude,
            longitude = entity.longitude,
            coverImage = null,
            externalId = "",
            timeZone = TimeZone.getTimeZone("UTC"), // unused downstream, never re-derived from this synthetic Place
            source = "",
        )
        pendingDataStore.set(
            PendingDataStore.Entry.Lodging(
                entityId = entity.id, selectedPlace = place, city = entity.city
            ),
        )
        val minCheckoutTime = entity.checkIn.toMidnight() + 1.days
        return ManualAddLodgingItemState(
            id = id,
            timestamp = entity.checkIn,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = entity.checkIn,
                minDateTime = null,
                isTimeSet = true,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = entity.name ?: entity.address,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = entity.checkout,
                minDateTime = minCheckoutTime,
                isTimeSet = true,
                dateSelectionEnabled = true,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = true,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override suspend fun onUpdated(state: ManualAddLodgingItemState): ManualAddLodgingItemState {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Lodging>()
        val selectedId = state.startState.selectedResultId
        val text = state.startState.locationText

        val resolved = if (selectedId != null && selectedId != entry.selectedPlace?.id) {
            val hotelDetails = repository.details(selectedId, autocompleteKey = state.id)
            val city = repository.placeCity(selectedId, autocompleteKey = state.id)
            val timeZone = (hotelDetails?.timeZone ?: city?.timeZone)?.toZoneId()
            pendingDataStore.update { e ->
                val lodging = e as PendingDataStore.Entry.Lodging
                lodging.copy(
                    selectedPlace = hotelDetails ?: lodging.selectedPlace,
                    city = city ?: lodging.city,
                )
            }
            val newCheckIn = timeZone?.let { state.startState.dateTime?.update(timeZone = it) }
                ?: state.startState.dateTime
            val newCheckOut = timeZone?.let { tz -> state.endState.dateTime?.update(timeZone = tz) }
                ?.takeIf { newCheckIn != null && it > newCheckIn } ?: state.endState.dateTime
            state.copy(
                timestamp = newCheckIn ?: state.timestamp,
                startState = state.startState.copy(
                    dateTime = newCheckIn,
                    locationText = hotelDetails?.name ?: hotelDetails?.address
                    ?: state.startState.locationText,
                    searchResults = emptyList(),
                ),
                endState = state.endState.copy(dateTime = newCheckOut),
            )
        } else if (selectedId == null && text != null && text.length >= 3) {
            val results = repository.autocomplete(text, autocompleteKey = state.id)
            state.copy(
                startState = state.startState.copy(
                    searchResults = results.map {
                        AutoCompleteResultState(
                            it.id, it.name, it.address
                        )
                    },
                ),
            )
        } else {
            state
        }
        val updatedEntry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Lodging>()
        val checkOut = resolved.endState.dateTime
        return resolved.copy(
            saveButtonEnabled = checkOut != null && checkOut > resolved.timestamp && updatedEntry.selectedPlace != null && resolved.startState.isTimeSet && resolved.endState.isTimeSet,
        )
    }

    override fun createEntity(item: ManualAddLodgingItemState): Lodging {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Lodging>()
        val place = entry.selectedPlace ?: error("lodging place is not set")
        val city = entry.city ?: error("lodging city is not set")
        val checkOut = item.endState.dateTime ?: error("checkout time is not set")
        return Lodging(
            id = entry.entityId ?: item.id,
            place.name,
            place.address,
            place.latitude,
            place.longitude,
            city,
            item.timestamp,
            checkOut,
        )
    }
}
