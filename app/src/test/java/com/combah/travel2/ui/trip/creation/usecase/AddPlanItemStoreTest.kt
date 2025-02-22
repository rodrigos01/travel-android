package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.data.Time
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.state.AddPlanItemState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class AddPlanItemStoreTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val dataFactory: AddPlanItemStore.DataFactory<PendingData> = mock()
    private val itemFactory: AddPlanItemStore.ItemFactory<AddPlanItemState, PendingData> =
        mock()

    private val subject = AddPlanItemStore(dataFactory, itemFactory)
    private val items = subject.items.stateIn(
        TestScope(rule.dispatcher), started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `createItem should return item from factory`() {
        val time = mock<Time>()
        val item = mockItemCreation(time)
        val result = subject.addItem(time)
        assertThat(result).isEqualTo(item)
    }

    @Test
    fun `createItem should add item to items`() {
        val time = mock<Time>()
        val item = mockItemCreation(time, "newItem")
        subject.addItem(time)
        assertThat(items.value["newItem"]).isEqualTo(item)
    }

    @Test
    fun `get should return data created by factory`() {
        val data = mock<PendingData> {
            on { id } doReturn "dataId"
        }
        dataFactory.stub { on { createData(any()) } doReturn data }
        subject.addItem(mock())
        val result = subject.get("dataId")
        assertThat(result).isEqualTo(data)
    }

    @Test
    fun `update should set item at itemId as item returned from factory by new data`() {
        val time = mock<Time>()
        mockItemCreation(time, "item")
        subject.addItem(time)

        val newData = mock<PendingData>()
        val newItem = mock<AddPlanItemState>()
        itemFactory.stub { on { createItem(newData, false) } doReturn newItem }
        subject.update("item") { newData }
        assertThat(items.value["item"]).isEqualTo(newItem)
    }

    @Test
    fun `remove should remove item from items`() {
        val time = mock<Time>()
        val item = mockItemCreation(time, "removed")
        subject.addItem(time)
        subject.remove(item)
        assertThat(items.value["removed"]).isNull()
    }

    private fun mockItemCreation(
        time: Time, itemId: String = "itemId"
    ): AddPlanItemState {
        val data = mock<PendingData> {
            on { id } doReturn itemId
        }
        val item = mock<AddPlanItemState> {
            on { id } doReturn itemId
        }
        dataFactory.stub { on { createData(time) } doReturn data }
        itemFactory.stub { on { createItem(data, false) } doReturn item }
        return item
    }
}
