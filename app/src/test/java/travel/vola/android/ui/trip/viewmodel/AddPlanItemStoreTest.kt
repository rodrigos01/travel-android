package travel.vola.android.ui.trip.viewmodel

import com.vola.android.extensions.get
import com.vola.android.test.UnconfinedDispatcherTestRule
import com.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import com.vola.android.ui.trip.creation.usecase.PendingData
import com.vola.android.ui.trip.state.AddPlanItemState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class AddPlanItemStoreTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val subject = AddPlanItemStore<PendingData, AddPlanItemState>()

    private var transformData: (PendingData, AddPlanUseCase.StateParams) -> AddPlanItemState =
        ::mockItemCreation
    private val items = subject.items(transformData).stateIn(
        TestScope(rule.dispatcher), started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `item in items should be result of transform`() {
        val data: PendingData = mock()
        val item = mock<AddPlanItemState>()
        transformData = { _, _ -> item }
        subject.addItem(data, stateParams = mock())
        assertThat(items["newItem"]).isEqualTo(item)
    }

    @Test
    fun `get data should return data added`() {
        val data = mock<PendingData> {
            on { id } doReturn "dataId"
        }
        subject.addItem(data, stateParams = mock())
        assertThat(subject.getData("dataId")).isEqualTo(data)
    }

    @Test
    fun `update should set data at itemId`() {
        subject.addItem(mock {
            on { id } doReturn "item"
        }, stateParams = mock())

        val newData = mock<PendingData>()
        subject.update("item") { newData }
        assertThat(subject.getData("item")).isEqualTo(newData)
    }

    @Test
    fun `remove should remove item from items`() {
        subject.addItem(mock {
            on { id } doReturn "removed"
        }, stateParams = mock())
        val item = items["removed"] ?: error("item not found")
        subject.remove(item)
        assertThat(items["removed"]).isNull()
    }

    private fun mockItemCreation(
        data: PendingData,
        params: AddPlanUseCase.StateParams,
    ): AddPlanItemState {
        val item = mock<AddPlanItemState> {
            on { id } doReturn data.id
            on { dateSelectionEnabled } doReturn params.dateSelectionEnabled
            on { typeSelectionEnabled } doReturn params.typeSelectionEnabled
            on { deleteButtonEnabled } doReturn params.deleteEnabled
        }
        return item
    }
}
