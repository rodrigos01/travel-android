package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.repository.PlaceAutoCompleteRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.creation.usecase.requireCurrent
import travel.vola.android.ui.trip.state.AddRestaurantItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime

class AddRestaurantUseCase(
    private val pendingDataStore: PendingDataStore,
    private val placeRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf("restaurant"),
    ),
) : AddPlanUseCase.AddItemUseCase<RestaurantReservation, AddRestaurantItemState>,
    AddPlanUseCase.EntityFactory<RestaurantReservation, AddRestaurantItemState> {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): AddRestaurantItemState {
        pendingDataStore.set(PendingDataStore.Entry.Restaurant(entityId = null, place = null, city = null))
        val start = time.toMidnight()
        return AddRestaurantItemState(
            id = id,
            timestamp = start,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = start,
                minDateTime = null,
                isTimeSet = false,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = null,
                minDateTime = null,
                isTimeSet = false,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = false,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override fun createItem(
        id: String,
        entity: RestaurantReservation,
        params: AddPlanUseCase.StateParams,
    ): AddRestaurantItemState {
        pendingDataStore.set(
            PendingDataStore.Entry.Restaurant(entityId = entity.id, place = entity.place, city = entity.city),
        )
        return AddRestaurantItemState(
            id = id,
            timestamp = entity.dateTime,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = entity.dateTime,
                minDateTime = null,
                isTimeSet = true,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = entity.place.name,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = null,
                minDateTime = null,
                isTimeSet = false,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = true,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override suspend fun onUpdated(state: AddRestaurantItemState): AddRestaurantItemState {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Restaurant>()
        val selectedId = state.startState.selectedResultId
        val text = state.startState.locationText
        val resolved = if (selectedId != null && selectedId != entry.place?.id) {
            val details = placeRepository.details(selectedId)
            val timeZone = (details?.place?.timeZone ?: details?.city?.timeZone)?.toZoneId()
            pendingDataStore.update { e ->
                (e as PendingDataStore.Entry.Restaurant).copy(
                    place = details?.place ?: e.place,
                    city = details?.city ?: e.city,
                )
            }
            val newTime = timeZone?.let { state.startState.dateTime?.update(timeZone = it) } ?: state.startState.dateTime
            state.copy(
                timestamp = newTime ?: state.timestamp,
                startState = state.startState.copy(
                    dateTime = newTime,
                    locationText = details?.place?.name ?: state.startState.locationText,
                    searchResults = emptyList(),
                ),
            )
        } else if (selectedId == null && text != null && text.length >= 3) {
            val results = placeRepository.autocomplete(text, state.id)
            state.copy(
                startState = state.startState.copy(
                    searchResults = results.map { AutoCompleteResultState(it.id, it.name, it.address) },
                ),
            )
        } else {
            state
        }
        val updatedEntry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Restaurant>()
        return resolved.copy(
            saveButtonEnabled = updatedEntry.place != null && updatedEntry.city != null && resolved.startState.isTimeSet,
        )
    }

    override fun createEntity(item: AddRestaurantItemState): RestaurantReservation {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.Restaurant>()
        return RestaurantReservation(
            id = entry.entityId ?: item.id,
            dateTime = item.timestamp,
            place = entry.place ?: error("Place not set"),
            city = entry.city ?: error("City not set"),
        )
    }
}
