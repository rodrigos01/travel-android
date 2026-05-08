package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import travel.vola.android.extensions.get
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import java.time.ZonedDateTime

class AddPlanItemStoreTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val subject = AddPlanItemStore<PendingData, AddPlanItemState>()

    private val testScope = TestScope(rule.dispatcher)

    @Test
    fun `item in items should be result of transform`() = runTest {
        val item = createAddPlanItemState("newItem")
        val items = subject.items { _, _ -> item }.stateIn(testScope)
        val data = createPendingData("newItem")
        subject.addItem(data, stateParams = mock())
        assertThat(items["newItem"]).isEqualTo(item)
    }

    @Test
    fun `get data should return data added`() {
        val data = createPendingData("dataId")
        subject.addItem(data, stateParams = mock())
        assertThat(subject.getData("dataId")).isEqualTo(data)
    }

    @Test
    fun `update should set data at itemId`() {
        subject.addItem(
            createPendingData("item"),
            stateParams = mock(),
        )

        val newData = createPendingData("item")
        subject.update("item") { newData }
        assertThat(subject.getData("item")).isEqualTo(newData)
    }

    @Test
    fun `remove should remove item from items`() = runTest {
        val items = subject.items { data, _ ->
            createAddPlanItemState(data.id)
        }.stateIn(testScope)
        subject.addItem(
            createPendingData("removed"),
            stateParams = mock(),
        )
        val item = items["removed"] ?: error("item not found")
        subject.remove(item)
        assertThat(items["removed"]).isNull()
    }

    private fun createPendingData(id: String) = PendingData.PendingFlight(
        id = id,
        departure = ZonedDateTime.now(),
    )

    private fun createAddPlanItemState(id: String) = AddFlightItemState(
        id = id,
        timestamp = ZonedDateTime.now(),
        typeSelectionEnabled = true,
        startState = mock(),
        endState = mock(),
        saveButtonEnabled = true,
        deleteButtonEnabled = true,
    )
}
