package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.PlaceAutoCompleteRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.AddRestaurantItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddRestaurantItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.time.ZonedDateTime

class AddRestaurantUseCase(
    private val coroutineScope: CoroutineScope,
    private val placeRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf(
            "restaurant"
        )
    ),
    private val itemStore: AddPlanItemStore<PendingData.PendingRestaurant, AddRestaurantItemState> = AddPlanItemStore(),
) : AddPlanUseCase.AddItemUseCase<RestaurantReservation, AddRestaurantItemState>,
    AddPlanUseCase.EntityFactory<RestaurantReservation, AddRestaurantItemState>,
    AddRestaurantItemActionHandler {

    override val items: MapFlow<String, AddRestaurantItemState> = itemStore.items(::createItem)

    override fun addItem(id: String, time: Time, params: AddPlanUseCase.StateParams) {
        itemStore.addItem(
            PendingData.PendingRestaurant(id, entityId = null, dateTime = time.toMidnight()),
            params,
        )
    }

    override fun addItem(
        id: String,
        entity: RestaurantReservation,
        params: AddPlanUseCase.StateParams
    ) {
        itemStore.addItem(
            PendingData.PendingRestaurant(
                id,
                entityId = entity.id,
                dateTime = entity.dateTime,
                place = entity.place,
                city = entity.city,
                searchResults = emptyList(),
            ),
            params,
        )
    }

    override fun removeItem(item: AddRestaurantItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        data: PendingData.PendingRestaurant, params: AddPlanUseCase.StateParams,
    ): AddRestaurantItemState {
        return AddRestaurantItemState(
            id = data.id,
            timestamp = data.dateTime,
            timeSelected = data.hasTime,
            saveButtonEnabled = data.place != null && data.city != null && data.hasTime,
            deleteButtonEnabled = params.deleteEnabled,
            typeSelectionEnabled = params.typeSelectionEnabled,
            dateSelectionEnabled = params.dateSelectionEnabled,
            restaurantName = data.place?.name,
            searchResults = data.searchResults.map {
                AutoCompleteResultState(
                    it.name, it.address
                )
            },
        )
    }

    override fun onUpdated(
        itemId: String,
        dateTime: ZonedDateTime?,
        timeSelected: Boolean,
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
                val timeZone = (details?.place?.timeZone ?: details?.city?.timeZone)?.toZoneId()
                    ?: it.dateTime.zone
                PendingData.PendingRestaurant(
                    id = it.id,
                    entityId = it.entityId,
                    dateTime = dateTime?.update(timeZone = timeZone) ?: it.dateTime,
                    hasTime = timeSelected,
                    searchResults = emptyList(),
                    place = details?.place ?: it.place,
                    city = details?.city ?: it.city,
                )
            }
        }
    }

    private val autoCompleteScope = MutexScope(coroutineScope.coroutineContext)
    override fun restaurantTextChanged(itemId: String, content: CharSequence) {
        autoCompleteScope.launch {
            val results = placeRepository.autocomplete(content.toString(), itemId)
            itemStore.update(itemId) {
                it.copy(searchResults = results)
            }
        }
    }

    override fun createEntity(item: AddRestaurantItemState): RestaurantReservation {
        val data = itemStore.getData(item.id) ?: error("Item ${item.id} not found in store")
        return RestaurantReservation(
            id = data.entityId ?: data.id,
            dateTime = data.dateTime,
            place = data.place ?: error("Place not set"),
            city = data.city ?: error("City not set"),
        )
    }
}