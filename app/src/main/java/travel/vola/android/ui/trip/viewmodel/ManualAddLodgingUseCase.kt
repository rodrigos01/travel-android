package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.days

class ManualAddLodgingUseCase(
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingLodging, ManualAddLodgingItemState> = AddPlanItemStore(),
    private val repository: LodgingSearchRepository = LodgingSearchRepository(),
) : AddPlanUseCase.AddItemUseCase<Lodging, ManualAddLodgingItemState>,
    AddPlanUseCase.EntityFactory<Lodging, ManualAddLodgingItemState>,
    ManualAddPlanItemActionHandler {

    override val items: MapFlow<String, ManualAddLodgingItemState> = itemStore.items(::createItem)

    override fun lodgingTextChanged(itemId: String, content: CharSequence) {
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

    override fun addItem(
        id: String,
        time: Time,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingLodging(
            id = id,
            checkIn = time.update(hour = 15, minute = 0),
            isCheckInTimeSet = true,
        )
        itemStore.addItem(data, params)
    }

    fun addItem(
        id: String,
        checkIn: ZonedDateTime,
        checkOut: ZonedDateTime?,
        params: AddPlanUseCase.StateParams,
    ) {
        val data = PendingLodging(
            id = id,
            checkIn = checkIn.update(hour = 15),
            isCheckInTimeSet = true,
            checkOut = checkOut?.update(hour = 10),
            isCheckOutTimeSet = checkOut != null,
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
            latitude = entity.latitude,
            longitude = entity.longitude,
            checkIn = entity.checkIn,
            isCheckInTimeSet = true,
            checkOut = entity.checkout,
            isCheckOutTimeSet = true,
            city = entity.city,
        )
        itemStore.addItem(data, params)
    }

    private fun createItem(
        data: PendingLodging,
        stateParams: AddPlanUseCase.StateParams,
    ): ManualAddLodgingItemState {
        val minCheckoutTime = data.checkIn.toMidnight() + 1.days
        return ManualAddLodgingItemState(
            id = data.id,
            timestamp = data.checkIn,
            startState = ManualAddPlanState(
                dateTime = data.checkIn,
                minDateTime = null,
                isTimeSet = data.isCheckInTimeSet,
                dateSelectionEnabled = stateParams.dateSelectionEnabled,
                locationText = data.name ?: data.address,
                searchResults = data.searchResults.map {
                    AutoCompleteResultState(
                        it.name, subtitle = it.address,
                    )
                },
            ),
            endState = ManualAddPlanState(
                dateTime = data.checkOut ?: minCheckoutTime.update(hour = 10, minute = 0),
                minDateTime = minCheckoutTime,
                isTimeSet = data.isCheckOutTimeSet,
                dateSelectionEnabled = true,
                locationText = null,
                searchResults = emptyList(),
            ),
            saveButtonEnabled = data.checkOut != null && data.checkOut > data.checkIn && (data.name
                ?: data.address) != null && data.isCheckInTimeSet && data.isCheckOutTimeSet,
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
        data.address ?: error("address is not set")
        data.latitude ?: error("lodging latitude is not set")
        data.longitude ?: error("lodging longitude is not set")
        data.city ?: error("lodging city is not set")
        data.checkOut ?: error("checkout time is not set")
        return Lodging(
            id = data.entityId ?: data.id,
            data.name,
            data.address,
            data.latitude,
            data.longitude,
            data.city,
            data.checkIn,
            data.checkOut,
        )
    }

    override fun onLodgingUpdated(
        itemId: String,
        checkIn: ZonedDateTime,
        checkInTimeSelected: Boolean,
        checkOut: ZonedDateTime?,
        checkOutTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) {
        val searchResults = itemStore.getData(itemId)?.searchResults
        val selected = searchResults?.getOrNull(selectedSearchResultIndex)
        if (selected != null) {
            itemStore.update(itemId) {
                it.copy(
                    city = null,
                    latitude = null,
                    longitude = null,
                )
            }
        }
        coroutineScope.launch {
            val (hotelDetails, city) = awaitAll(
                async { selected?.id?.let { repository.details(it, autocompleteKey = itemId) } },
                async { selected?.id?.let { repository.placeCity(it, autocompleteKey = itemId) } },
            )
            itemStore.update(itemId) { data ->
                val timeZone =
                    (hotelDetails?.timeZone ?: city?.timeZone ?: data.city?.timeZone)?.toZoneId()
                        ?: data.checkIn.zone
                PendingLodging(
                    id = data.id,
                    entityId = data.entityId,
                    checkIn = timeZone?.let { checkIn.update(timeZone = it) } ?: checkIn,
                    isCheckInTimeSet = checkInTimeSelected,
                    checkOut = timeZone?.let { checkOut?.update(timeZone = it) },
                    isCheckOutTimeSet = checkOutTimeSelected,
                    name = hotelDetails?.name ?: data.name,
                    address = hotelDetails?.address ?: data.address,
                    city = city ?: data.city,
                    latitude = hotelDetails?.latitude ?: data.latitude,
                    longitude = hotelDetails?.longitude ?: data.longitude,
                    searchResults = emptyList(),
                )
            }
        }
    }
}
