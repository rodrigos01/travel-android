package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Time
import com.combah.travel2.test.assertType
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase.AddPlanItem
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddPlanUseCaseTest {

    private val addFlightUseCase: AddFlightUseCase = mock()
    private val addLodgingUseCase: AddLodgingUseCase = mock()
    private val subject = AddPlanUseCase(addFlightUseCase, addLodgingUseCase)

    @Test
    fun `added plan item should be initialized as Flight`() {
        val original = mock<AddFlightUseCase.AddFlightItem>()
        addFlightUseCase.stub {
            on { createItem(any()) } doReturn original
        }
        val addedItem = subject.createAddPlanItem(mock())
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
    }

    @Test
    fun `Add Plan Items should have all types available`() {
        val original = AddFlightUseCase.AddFlightItem(id = "originalItem", timestamp = mock())
        assertThat(original.types).containsExactly(
            AddPlanItem.Type.Flight,
            AddPlanItem.Type.Lodging,
        )
    }

    @Test
    fun `added flight item should be initialized with initial time as departure`() {
        val expected: AddFlightUseCase.AddFlightItem = mock()
        val initialTime: Time = mock()
        addFlightUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val addedItem = subject.createAddPlanItem(initialTime)
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should change item`() {
        val expected: AddLodgingUseCase.AddLodgingItem = mock()
        val initialTime: Time = mock()
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
            on { timestamp } doReturn initialTime
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Lodging)
        assertThat(newItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should keep original item's time`() {
        val initialTime: Time = mock()
        val expected: AddLodgingUseCase.AddLodgingItem = mock {
            on { timestamp } doReturn initialTime
        }
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
            on { timestamp } doReturn initialTime
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val addedItem = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(addedItem, AddPlanItem.Type.Lodging)
        assertType<AddLodgingUseCase.AddLodgingItem>(newItem)
        assertThat(newItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should not change item change item if same item selected`() {
        val expected: AddLodgingUseCase.AddLodgingItem = mock()
        val initialTime: Time = mock()
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Flight)
        assertThat(newItem).isEqualTo(item)
    }
}