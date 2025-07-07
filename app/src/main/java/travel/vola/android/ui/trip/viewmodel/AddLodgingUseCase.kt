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
            itemId, item.timestamp, AddPlanUseCase.StateParams(
                item.dateSelectionEnabled, item.typeSelectionEnabled, item.deleteButtonEnabled
            )
        )
    }

    override fun onFindLodgingButtonTapped(itemId: String) {
        val item = items[itemId] ?: return
        removeItem(item)
        lodgingSearchParamsUseCase.addItem(
            itemId, item.timestamp, AddPlanUseCase.StateParams(
                item.dateSelectionEnabled, item.typeSelectionEnabled, item.deleteButtonEnabled
            )
        )
    }

    override val items: MapStateFlow<String, AddLodgingItemState> = mergeMaps(
        manualAddLodgingUseCase.items,
        lodgingSearchParamsUseCase.items,
    ).stateIn(coroutineScope, SharingStarted.Eagerly, emptyMap())

    override fun setCheckInTime(itemId: String, time: Time) =
        getActionHandler(itemId).setCheckInTime(itemId, time)

    override fun setCheckOutTime(itemId: String, time: Time) =
        getActionHandler(itemId).setCheckOutTime(itemId, time)

    override fun lodgingTextChanged(itemId: String, content: CharSequence) =
        getActionHandler(itemId).lodgingTextChanged(itemId, content)

    override fun lodgingSearchResultTapped(itemId: String, index: Int) =
        getActionHandler(itemId).lodgingSearchResultTapped(itemId, index)

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
