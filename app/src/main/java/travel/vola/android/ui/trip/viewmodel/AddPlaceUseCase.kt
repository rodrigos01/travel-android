package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.toMidnight
import travel.vola.android.model.data.Time
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.repository.PlaceAutoCompleteRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlaceItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.time.ZonedDateTime

class AddPlaceUseCase(
    private val coroutineScope: CoroutineScope,
    private val placeRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf(
            "city", "point_of_interest"
        )
    ),
    private val itemStore: AddPlanItemStore<PendingData.PendingTimedPlace, AddPlaceItemState> = AddPlanItemStore(),
) : AddPlanUseCase.AddItemUseCase<TimedPlace, AddPlaceItemState>,
    AddPlanUseCase.EntityFactory<TimedPlace, AddPlaceItemState>, AddPlaceItemActionHandler {

    override val items: MapFlow<String, AddPlaceItemState> = itemStore.items(::createItem)

    override fun addItem(id: String, time: Time, params: AddPlanUseCase.StateParams) {
        itemStore.addItem(
            PendingData.PendingTimedPlace(id, entityId = null, startDateTime = time.toMidnight()),
            params,
        )
    }

    override fun addItem(id: String, entity: TimedPlace, params: AddPlanUseCase.StateParams) {
        itemStore.addItem(
            PendingData.PendingTimedPlace(
                id,
                entity.id,
                entity.startDateTime,
                entity.hasStartTime,
                entity.endDateTime,
                entity.hasEndTime,
                entity.place,
                entity.city,
            ),
            params,
        )
    }

    override fun removeItem(item: AddPlaceItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        data: PendingData.PendingTimedPlace, params: AddPlanUseCase.StateParams,
    ): AddPlaceItemState {
        return AddPlaceItemState(
            id = data.id,
            timestamp = data.startDateTime,
            startTimeSelected = data.hasStartTime,
            endDateTime = data.endDateTime,
            endTimeSelected = data.hasEndTime,
            minEndTime = data.startDateTime,
            saveButtonEnabled = data.place != null && data.city != null && (data.endDateTime == null || data.endDateTime >= data.startDateTime),
            deleteButtonEnabled = params.deleteEnabled,
            typeSelectionEnabled = params.typeSelectionEnabled,
            dateSelectionEnabled = params.dateSelectionEnabled,
            placeName = data.place?.name,
            searchResults = data.searchResults.map {
                AutoCompleteResultState(
                    it.name, it.address
                )
            },
        )
    }

    override fun onUpdated(
        itemId: String,
        startDateTime: ZonedDateTime?,
        startTimeSelected: Boolean,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) {
        val current = itemStore.getData(itemId)
        val selected = current?.searchResults?.getOrNull(selectedSearchResultIndex)
        if (selected != null) {
            itemStore.update(itemId) {
                it.copy(
                    place = null,
                    city = null,
                )
            }
        }
        coroutineScope.launch {
            val details = selected?.id?.let { selectedId -> placeRepository.details(selectedId) }
            itemStore.update(itemId) {
                PendingData.PendingTimedPlace(
                    id = it.id,
                    entityId = it.entityId,
                    startDateTime = startDateTime ?: it.startDateTime,
                    hasStartTime = startTimeSelected,
                    endDateTime = endDateTime,
                    hasEndTime = endTimeSelected,
                    searchResults = emptyList(),
                    place = details?.place,
                    city = details?.city,
                )
            }
        }
    }

    private val autoCompleteScope = MutexScope(coroutineScope.coroutineContext)
    override fun locationTextChanged(itemId: String, content: CharSequence) {
        autoCompleteScope.launch {
            val results = placeRepository.autocomplete(content.toString(), itemId)
            itemStore.update(itemId) {
                it.copy(searchResults = results)
            }
        }
    }

    override fun createEntity(item: AddPlaceItemState): TimedPlace {
        val data = itemStore.getData(item.id) ?: error("Item ${item.id} not found in store")
        return TimedPlace(
            id = data.entityId ?: data.id,
            startDateTime = data.startDateTime,
            hasStartTime = data.hasStartTime,
            endDateTime = data.endDateTime,
            hasEndTime = data.hasEndTime,
            place = data.place ?: error("Place not set"),
            city = data.city ?: error("City not set"),
        )
    }
}