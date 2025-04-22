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
        itemStore.addItem(PendingData.PendingTimedPlace(id, time.toMidnight()), params)
    }

    override fun addItem(id: String, entity: TimedPlace, params: AddPlanUseCase.StateParams) {
        itemStore.addItem(
            PendingData.PendingTimedPlace(
                id, entity.dateTime, entity.hasTime, entity.place, entity.city
            ), params
        )
    }

    override fun removeItem(item: AddPlaceItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        data: PendingData.PendingTimedPlace, params: AddPlanUseCase.StateParams
    ): AddPlaceItemState {
        return AddPlaceItemState(
            id = data.id,
            timestamp = data.dateTime,
            saveButtonEnabled = data.place != null && data.city != null,
            deleteButtonEnabled = params.deleteEnabled,
            typeSelectionEnabled = params.typeSelectionEnabled,
            dateSelectionEnabled = params.dateSelectionEnabled,
            timeSelected = data.hasTime,
            placeName = data.place?.name,
            searchResults = data.searchResults.map {
                AutoCompleteResultState(
                    it.name, it.address
                )
            },
        )
    }

    override fun setPlaceArrivalDateTime(itemId: String, time: Time, timeSelected: Boolean) {
        itemStore.update(itemId) {
            it.copy(
                dateTime = time,
                hasTime = timeSelected,
            )
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

    override fun locationSearchResultTapped(itemId: String, index: Int) {
        val selected = itemStore.getData(itemId)?.searchResults?.getOrNull(index) ?: return
        coroutineScope.launch {
            val details = placeRepository.details(selected.id) ?: return@launch
            itemStore.update(itemId) {
                it.copy(
                    place = details.place,
                    city = details.city,
                    searchResults = emptyList(),
                )
            }
        }
    }

    override fun createEntity(item: AddPlaceItemState): TimedPlace {
        val data = itemStore.getData(item.id) ?: error("Item ${item.id} not found in store")
        return TimedPlace(
            id = data.id,
            dateTime = data.dateTime,
            hasTime = data.hasTime,
            place = data.place ?: error("Place not set"),
            city = data.city ?: error("City not set"),
        )
    }
}