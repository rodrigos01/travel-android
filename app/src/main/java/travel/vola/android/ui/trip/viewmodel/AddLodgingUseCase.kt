package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Lodging
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.state.AddLodgingItemState
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.ZonedDateTime

class AddLodgingUseCase(
    placeRepository: PlaceRepository,
    pendingDataStore: PendingDataStore,
    private val manualAddLodgingUseCase: ManualAddLodgingUseCase = ManualAddLodgingUseCase(
        pendingDataStore = pendingDataStore,
    ),
    private val lodgingSearchParamsUseCase: LodgingSearchParamsUseCase = LodgingSearchParamsUseCase(
        pendingDataStore = pendingDataStore,
        placeRepository = placeRepository,
    ),
) : AddPlanUseCase.AddItemUseCase<Lodging, AddLodgingItemState>,
    AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState> by manualAddLodgingUseCase,
    LodgingSearchParamsFactory by lodgingSearchParamsUseCase {

    override fun createItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams,
    ): AddLodgingItemState = manualAddLodgingUseCase.createItem(id, time, params)

    override fun createItem(
        id: String,
        entity: Lodging,
        params: AddPlanUseCase.StateParams,
    ): AddLodgingItemState = manualAddLodgingUseCase.createItem(id, entity, params)

    override suspend fun onUpdated(state: AddLodgingItemState): AddLodgingItemState = when (state) {
        is ManualAddLodgingItemState -> manualAddLodgingUseCase.onUpdated(state)
        is LodgingSearchItemState -> lodgingSearchParamsUseCase.onUpdated(state)
    }

    fun switchToManual(current: LodgingSearchItemState): ManualAddLodgingItemState =
        manualAddLodgingUseCase.createItem(
            id = current.id,
            checkIn = current.checkIn,
            checkOut = current.checkOut,
            params = AddPlanUseCase.StateParams(
                current.dateSelectionEnabled,
                current.typeSelectionEnabled,
                current.deleteButtonEnabled,
            ),
        )

    fun switchToSearch(current: ManualAddLodgingItemState): LodgingSearchItemState =
        lodgingSearchParamsUseCase.createItem(
            id = current.id,
            checkIn = current.startState.dateTime ?: current.timestamp,
            checkOut = current.endState.dateTime,
            city = null, // TODO: Use city from item when Unified Places API is available
            params = AddPlanUseCase.StateParams(
                current.dateSelectionEnabled,
                current.typeSelectionEnabled,
                current.deleteButtonEnabled,
            ),
        )
}
