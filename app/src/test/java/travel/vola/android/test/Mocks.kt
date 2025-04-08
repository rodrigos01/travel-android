package travel.vola.android.test

import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import travel.vola.android.extensions.get
import travel.vola.android.extensions.set
import travel.vola.android.model.data.Time
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.viewmodel.AddPlanUseCase
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object Mocks {
    @Deprecated("use the Time(String) constructor instead")
    fun mockTime(stubbing: KStubbing<Time>.(Time) -> Unit = {}): Time {
        val instant: Instant = mock {
            on { toEpochMilli() } doReturn 0L
        }
        val localTime = mock<LocalTime> {
            on { nano } doReturn 0
        }
        return mock {
            on { zone } doReturn ZoneId.systemDefault()
            on { minus(any<Duration>()) } doReturn it
            on { plus(any<Duration>()) } doReturn it
            on { toInstant() } doReturn instant
            on { withHour(0) } doReturn it
            on { toLocalTime() } doReturn localTime
            stubbing(it)
        }
    }

    inline fun <reified R, T> mockItemStore(): AddPlanItemStore<R, T> {
        val dataFlow = MutableStateFlow<Map<String, R>>(emptyMap())
        val itemFlow = MutableStateFlow<Map<String, T>>(emptyMap())
        return mock<AddPlanItemStore<R, T>> {
            val captor =
                argumentCaptor<(R, AddPlanUseCase.StateParams) -> T>()
            on { items(captor.capture()) } doReturn itemFlow
            on { addItem(any(), any(), any()) } doAnswer {
                val key = it.getArgument<String>(0)
                val data = it.getArgument<R>(1)
                val params = it.getArgument<AddPlanUseCase.StateParams>(2)
                dataFlow[key] = data
                itemFlow[key] = captor.firstValue(data, params)
            }
            on { update(any(), any()) } doAnswer {
                val id = it.getArgument<String>(0)
                val updater = it.getArgument<(R) -> R>(1)
                dataFlow[id]?.let { data ->
                    val updated = updater(data)
                    dataFlow[id] = updated
                    itemFlow[id] = captor.firstValue(updated, mock())
                }
            }
        }
    }
}
