package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.repository.PlaceAutoCompleteRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.creation.usecase.requireCurrent
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import java.time.ZonedDateTime

class AddPlaceUseCase(
    private val pendingDataStore: PendingDataStore,
    private val placeRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf("city", "point_of_interest"),
    ),
) : AddPlanUseCase.AddItemUseCase<TimedPlace, AddPlaceItemState>,
    AddPlanUseCase.EntityFactory<TimedPlace, AddPlaceItemState> {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): AddPlaceItemState {
        pendingDataStore.set(PendingDataStore.Entry.TimedPlace(entityId = null, place = null, city = null))
        val start = time.toMidnight()
        return AddPlaceItemState(
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
                minDateTime = start,
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
        entity: TimedPlace,
        params: AddPlanUseCase.StateParams,
    ): AddPlaceItemState {
        pendingDataStore.set(
            PendingDataStore.Entry.TimedPlace(entityId = entity.id, place = entity.place, city = entity.city),
        )
        return AddPlaceItemState(
            id = id,
            timestamp = entity.startDateTime,
            typeSelectionEnabled = params.typeSelectionEnabled,
            startState = ManualAddPlanState(
                dateTime = entity.startDateTime,
                minDateTime = null,
                isTimeSet = entity.hasStartTime,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = entity.place.name,
                searchResults = emptyList(),
            ),
            endState = ManualAddPlanState(
                dateTime = entity.endDateTime,
                minDateTime = entity.startDateTime,
                isTimeSet = entity.hasEndTime,
                dateSelectionEnabled = params.dateSelectionEnabled,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = true,
            deleteButtonEnabled = params.deleteEnabled,
        )
    }

    override suspend fun onUpdated(state: AddPlaceItemState): AddPlaceItemState {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.TimedPlace>()
        val selectedId = state.startState.selectedResultId
        val text = state.startState.locationText
        val resolved = if (selectedId != null && selectedId != entry.place?.id) {
            val details = placeRepository.details(selectedId)
            val timeZone = (details?.place?.timeZone ?: details?.city?.timeZone)?.toZoneId()
            pendingDataStore.update { e ->
                (e as PendingDataStore.Entry.TimedPlace).copy(
                    place = details?.place ?: e.place,
                    city = details?.city ?: e.city,
                )
            }
            val newStart = timeZone?.let { state.startState.dateTime?.update(timeZone = it) } ?: state.startState.dateTime
            val newEnd = timeZone?.let { tz -> state.endState.dateTime?.update(timeZone = tz) } ?: state.endState.dateTime
            state.copy(
                timestamp = newStart ?: state.timestamp,
                startState = state.startState.copy(
                    dateTime = newStart,
                    locationText = details?.place?.name ?: state.startState.locationText,
                    searchResults = emptyList(),
                ),
                endState = state.endState.copy(dateTime = newEnd),
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
        val updatedEntry = pendingDataStore.requireCurrent<PendingDataStore.Entry.TimedPlace>()
        val endDateTime = resolved.endState.dateTime
        return resolved.copy(
            saveButtonEnabled = updatedEntry.place != null && updatedEntry.city != null &&
                (endDateTime == null || endDateTime >= resolved.timestamp),
        )
    }

    override fun createEntity(item: AddPlaceItemState): TimedPlace {
        val entry = pendingDataStore.requireCurrent<PendingDataStore.Entry.TimedPlace>()
        return TimedPlace(
            id = entry.entityId ?: item.id,
            startDateTime = item.timestamp,
            hasStartTime = item.startState.isTimeSet,
            endDateTime = item.endState.dateTime,
            hasEndTime = item.endState.isTimeSet,
            place = entry.place ?: error("Place not set"),
            city = entry.city ?: error("City not set"),
        )
    }
}
