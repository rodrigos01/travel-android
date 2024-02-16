package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.test.assertType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddLodgingUseCaseTest {
    private val formatter: TimeFormatter = mock {
        on { dayOfMonthString(any()) } doReturn ""
        on { dayOfWeekString(any()) } doReturn ""
    }
    private val repository: AddLodgingRepository = mock()
    private val subject = AddLodgingUseCase(repository, formatter)

    @Test
    fun `created item should be initialized empty`() {
        val addedItem = subject.createItem(mock())
        assertType<AddLodgingUseCase.AddLodgingItem>(addedItem)
        assertThat(addedItem.name).isNull()
        assertThat(addedItem.checkOutTime).isNull()
    }

    @Test
    fun `created item should be initialized with initial time as check-in`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
            on { dayOfWeekString(initialTime) } doReturn "Fri"
            on { dayOfMonthString(initialTime) } doReturn "16"
        }
        val addedItem = subject.createItem(initialTime)
        assertType<AddLodgingUseCase.AddLodgingItem>(addedItem)
        assertThat(addedItem.checkInTime).isEqualTo("6:15")
        assertThat(addedItem.checkOutDayOfMonth).isEqualTo("16")
        assertThat(addedItem.checkOutDayOfWeek).isEqualTo("Fri")
    }

    @Test
    fun `lodging text changed should trigger repository autocomplete`() = runTest {
        repository.stub {
            onBlocking { autocomplete("hil") } doReturn emptyList()
        }
        val original = subject.createItem(mock())
        subject.lodgingTextChanged(original.id, "hil")
        verify(repository).autocomplete("hil")
    }

    @Test
    fun `airport from search text changed should update item with repository results`() = runTest {
        val expected = listOf(
            "Hilton NYC",
            "Hilton New Jersey",
            "Paris Hilton",
        )
        val results = expected.map { lodgingName ->
            mock<Lodging> {
                on { name } doReturn lodgingName
            }
        }
        repository.stub {
            onBlocking { autocomplete("hil") } doReturn results
        }
        val original = subject.createItem(mock())
        subject.lodgingTextChanged(original.id, "hil")
        val newItem = subject.items.value[original.id]
        assertThat(newItem?.lodgingSearchResults).isEqualTo(expected)
    }

    @Test
    fun `airport from search result tapped should update item with selected airport`() = runTest {
        val expected = listOf(
            "Hilton NYC",
            "Hilton New Jersey",
            "Paris Hilton",
        )
        val results = expected.map { lodgingName ->
            mock<Lodging> {
                on { name } doReturn lodgingName
            }
        }
        repository.stub {
            onBlocking { autocomplete("hil") } doReturn results
        }
        val original = subject.createItem(mock())
        subject.lodgingTextChanged(original.id, "hil")
        val newItem =
            subject.items.value[original.id] ?: Assertions.fail("no item after search text changed")
        subject.lodgingSearchResultTapped(newItem.id, 1)
        val selectedItem = subject.items.value[original.id]
        assertThat(selectedItem?.name).isEqualTo("Hilton New Jersey")
    }
}