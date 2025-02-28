package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.MapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.mergeMaps
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.AddLodgingItemActionHandlerBase
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.LodgingSearchItemState
import com.combah.travel2.ui.trip.state.ManualAddLodgingItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AddLodgingUseCase(
    private val coroutineScope: CoroutineScope,
    private val manualAddLodgingUseCase: ManualAddLodgingUseCase = ManualAddLodgingUseCase(
        coroutineScope = coroutineScope
    ),
    private val lodgingSearchParamsUseCase: LodgingSearchParamsUseCase = LodgingSearchParamsUseCase(
        coroutineScope = coroutineScope
    ),
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState>,
    AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState> by manualAddLodgingUseCase,
    AddLodgingItemActionHandler {

    override fun onSwitchToManualButtonTapped(itemId: String) {
        val item = items[itemId] ?: return
        removeItem(item)
        manualAddLodgingUseCase.addItem(
            item.timestamp,
            AddPlanUseCase.StateParams(
                item.dateSelectionEnabled,
                item.typeSelectionEnabled,
                item.deleteButtonEnabled
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

    override fun locationTextChanged(itemId: String, content: CharSequence) =
        getActionHandler(itemId).locationTextChanged(itemId, content)

    override fun locationSearchResultTapped(itemId: String, index: Int) =
        getActionHandler(itemId).locationSearchResultTapped(itemId, index)

    override fun addItem(time: Time, params: AddPlanUseCase.StateParams): AddPlanItemState =
        lodgingSearchParamsUseCase.addItem(time, params)

    override fun addItem(entity: Lodging, params: AddPlanUseCase.StateParams): AddLodgingItemState =
        manualAddLodgingUseCase.addItem(entity, params)

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
