package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Time
import com.combah.travel2.test.assertType
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase.AddPlanItem
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddPlanUseCaseTest {

    private val formatter: TimeFormatter = mock()
    private val subject = AddPlanUseCase(formatter)

    @Test
    fun `added plan item should be initialized as empty Flight`() {
        val addedItem = subject.createAddPlanItem(mock())
        assertType<AddPlanItem.Flight>(addedItem)
        assertThat(addedItem.airportFromName).isNull()
        assertThat(addedItem.arrivalDayOfMonth).isNull()
        assertThat(addedItem.arrivalDayOfWeek).isNull()
        assertThat(addedItem.arrivalTime).isNull()
        assertThat(addedItem.airportToName).isNull()
    }

    @Test
    fun `added flight item should be initialized with all types available`() {
        val addedItem = subject.createAddPlanItem(mock())
        assertType<AddPlanItem.Flight>(addedItem)
        assertThat(addedItem.types).containsExactly(
            AddPlanItem.Type.Flight,
            AddPlanItem.Type.Lodging,
        )
    }

    @Test
    fun `added flight item should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
        }
        val addedItem = subject.createAddPlanItem(initialTime)
        assertType<AddPlanItem.Flight>(addedItem)
        assertThat(addedItem.departureTime).isEqualTo("6:15")
    }

    @Test
    fun `type selected should change item`() {
        val initialTime: Time = mock()
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Lodging)
        assertType<AddPlanItem.Lodging>(newItem)
    }

    @Test
    fun `type selected should keep original item's time`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
        }
        val addedItem = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(addedItem, AddPlanItem.Type.Lodging)
        assertType<AddPlanItem.Lodging>(newItem)
        assertThat(newItem.checkInTime).isEqualTo("6:15")
    }

    @Test
    fun `type selected should not change item change item if same item selected`() {
        val initialTime: Time = mock()
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Flight)
        assertThat(newItem).isEqualTo(item)
    }
}