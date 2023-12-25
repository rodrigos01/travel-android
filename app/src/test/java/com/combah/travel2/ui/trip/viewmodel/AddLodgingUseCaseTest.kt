package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Time
import com.combah.travel2.test.assertType
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddLodgingUseCaseTest {
    private val formatter: TimeFormatter = mock()
    private val subject = AddLodgingUseCase(formatter)

    @Test
    fun `created item should be initialized empty`() {
        val addedItem = subject.createItem(mock())
        assertType<AddLodgingUseCase.AddLodgingItem>(addedItem)
        Assertions.assertThat(addedItem.name).isNull()
        Assertions.assertThat(addedItem.checkOutDayOfMonth).isNull()
        Assertions.assertThat(addedItem.checkOutDayOfWeek).isNull()
        Assertions.assertThat(addedItem.checkOutTime).isNull()
    }

    @Test
    fun `created item should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
        }
        val addedItem = subject.createItem(initialTime)
        assertType<AddLodgingUseCase.AddLodgingItem>(addedItem)
        Assertions.assertThat(addedItem.checkInTime).isEqualTo("6:15")
    }
}