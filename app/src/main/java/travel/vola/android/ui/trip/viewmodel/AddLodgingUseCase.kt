package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import travel.vola.android.extensions.MapStateFlow
import travel.vola.android.extensions.get
import travel.vola.android.extensions.mergeMaps
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Time
import travel.vola.android.ui.trip.creation.usecase.AddLodgingItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddLodgingItemActionHandlerBase
import travel.vola.android.ui.trip.state.AddLodgingItemState
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.ZonedDateTime

class AddLodgingUseCase(
    placeRepository: PlaceRepository,
    private val coroutineScope: CoroutineScope,
    private val manualAddLodgingUseCase: ManualAddLodgingUseCase = ManualAddLodgingUseCase(
        coroutineScope = coroutineScope
    ),
    private val lodgingSearchParamsUseCase: LodgingSearchParamsUseCase = LodgingSearchParamsUseCase(
        placeRepository = placeRepository, coroutineScope = coroutineScope
    ),
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState>,
    AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState> by manualAddLodgingUseCase,
    LodgingSearchParamsFactory by lodgingSearchParamsUseCase, AddLodgingItemActionHandler {

    override fun onSwitchToManualButtonTapped(itemId: String) {
        val item = items[itemId] ?: return
        removeItem(item)
        manualAddLodgingUseCase.addItem(
            id = itemId,
            checkIn = item.timestamp,
            checkOut = item.checkOut,
            params = AddPlanUseCase.StateParams(
                item.dateSelectionEnabled, item.typeSelectionEnabled, item.deleteButtonEnabled
            ),
        )
    }

    override fun onFindLodgingButtonTapped(itemId: String) {
        val item = items[itemId] ?: return
        removeItem(item)
        lodgingSearchParamsUseCase.addItem(
            id = itemId,
            checkIn = item.timestamp,
            checkOut = item.checkOut,
            city = null, // TODO: Use city from item when Unified Places API is available
            params = AddPlanUseCase.StateParams(
                item.dateSelectionEnabled, item.typeSelectionEnabled, item.deleteButtonEnabled
            ),
        )
    }

    override val items: MapStateFlow<String, AddLodgingItemState> = mergeMaps(
        manualAddLodgingUseCase.items,
        lodgingSearchParamsUseCase.items,
    ).stateIn(coroutineScope, SharingStarted.Eagerly, emptyMap())

    override fun lodgingTextChanged(itemId: String, content: CharSequence) =
        getActionHandler(itemId).lodgingTextChanged(itemId, content)

    override fun onLodgingUpdated(
        itemId: String,
        checkIn: ZonedDateTime,
        checkInTimeSelected: Boolean,
        checkOut: ZonedDateTime?,
        checkOutTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) {
        getActionHandler(itemId).onLodgingUpdated(
            itemId,
            checkIn,
            checkInTimeSelected,
            checkOut,
            checkOutTimeSelected,
            selectedSearchResultIndex,
        )
    }

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ) = lodgingSearchParamsUseCase.addItem(id, time, params)

    override fun addItem(id: String, entity: Lodging, params: AddPlanUseCase.StateParams) =
        manualAddLodgingUseCase.addItem(id, entity, params)

    override fun removeItem(item: AddLodgingItemState) = when (item) {
        is ManualAddLodgingItemState -> manualAddLodgingUseCase.removeItem(item)
        is LodgingSearchItemState -> lodgingSearchParamsUseCase.removeItem(item)
    }

    private fun getActionHandler(itemId: String): AddLodgingItemActionHandlerBase =
        items[itemId]?.let { item ->
            when (item) {
                is ManualAddLodgingItemState -> manualAddLodgingUseCase
                is LodgingSearchItemState -> lodgingSearchParamsUseCase
            }
        } ?: error("Item with id $itemId not found in store")
}
