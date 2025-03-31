package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.plus
import travel.vola.android.extensions.toMidnight
import travel.vola.android.extensions.update
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.ManualAddPlanItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingLodging
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import kotlin.time.Duration.Companion.days

class ManualAddLodgingUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingLodging, ManualAddLodgingItemState> = AddPlanItemStore(),
    private val repository: LodgingSearchRepository = LodgingSearchRepository(),
) : AddPlanUseCase.AddItemUseCase<Lodging, ManualAddLodgingItemState>,
    AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState>,
    ManualAddPlanItemActionHandler {

    override val items: MapFlow<String, ManualAddLodgingItemState> = itemStore.items(::createItem)

    override fun setCheckInTime(itemId: String, time: Time) = itemStore.update(itemId) {
        it.copy(
            checkIn = it.checkIn.update(
                dayOfMonth = time.dayOfMonth,
                month = time.month,
                year = time.year,
                hour = time.hour,
                minute = time.minute,
            )
        )
    }

    override fun setCheckOutTime(itemId: String, time: Time) {
        itemStore.update(itemId) {
            it.copy(
                checkOut = it.checkOut.update(
                    dayOfMonth = time.dayOfMonth,
                    month = time.month,
                    year = time.year,
                    hour = time.hour,
                    minute = time.minute,
                )
            )
        }
    }

    override fun locationTextChanged(itemId: String, content: CharSequence) {
        if (content.length < 3) {
            return
        }
        coroutineScope.launch {
            val results = repository.autocomplete(content.toString(), autocompleteKey = itemId)
            itemStore.update(itemId) { data ->
                data.copy(
                    searchResults = results
                )
            }
        }
    }

    override fun locationSearchResultTapped(itemId: String, index: Int) {
        val selected = itemStore.getData(itemId)?.searchResults?.getOrNull(index) ?: return
        itemStore.update(itemId) { data ->
            data.copy(
                name = selected.name,
                address = selected.address,
                searchResults = emptyList(),
            )
        }
        coroutineScope.launch {
            val city = repository.placeCity(selected.id, autocompleteKey = itemId)
            itemStore.update(itemId) { data ->
                data.copy(city = city)
            }
        }
    }

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingLodging(
            id = id,
            checkIn = time,
            checkOut = time.toMidnight() + 1.days,
        )
        itemStore.addItem(data, params)
    }

    override fun addItem(
        id: String,
        entity: Lodging,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingLodging(
            id = id,
            entityId = entity.id,
            name = entity.name,
            address = entity.address,
            checkIn = entity.checkIn,
            checkOut = entity.checkout,
            city = entity.city,
        )
        itemStore.addItem(data, params)
    }

    private fun createItem(
        data: PendingLodging,
        stateParams: AddPlanUseCase.StateParams,
    ): ManualAddLodgingItemState {
        return ManualAddLodgingItemState(
            id = data.id,
            timestamp = data.checkIn,
            startState = ManualAddPlanState(
                time = data.checkIn,
                minTime = data.checkIn.toMidnight(),
                dateSelectionEnabled = stateParams.dateSelectionEnabled,
                locationText = data.name ?: data.address,
                searchResults = data.searchResults.map {
                    AutoCompleteResultState(
                        it.name, subtitle = it.address,
                    )
                },
            ),
            endState = ManualAddPlanState(
                time = data.checkOut,
                minTime = data.checkIn.toMidnight() + 1.days,
                dateSelectionEnabled = true,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = data.checkOut > data.checkIn && (data.name ?: data.address) != null,
            deleteButtonEnabled = stateParams.deleteEnabled,
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
        )
    }

    override fun removeItem(item: ManualAddLodgingItemState) {
        itemStore.remove(item)
    }

    override fun createEntity(item: ManualAddLodgingItemState): Lodging {
        val data =
            itemStore.getData(item.id) ?: error("item has no pending data associated with it")
        data.address ?: error("address from is not set")
        data.city ?: error("lodging city is not set")
        data.checkOut
        return Lodging(
            id = data.entityId ?: data.id,
            item.startState.locationText,
            data.address,
            data.city,
            data.checkIn,
            data.checkOut,
        )
    }
}
