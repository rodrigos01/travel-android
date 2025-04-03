package travel.vola.android.test

import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import travel.vola.android.model.data.Time
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

object Mocks {
    fun mockTime(stubbing: KStubbing<Time>.(Time) -> Unit = {}): Time {
        val timeZoneMock: ZoneId = mock()
        val instant: Instant = mock {
            on { toEpochMilli() } doReturn 0L
        }
        return mock {
            on { zone } doReturn timeZoneMock
            on { minus(any<Duration>()) } doReturn it
            on { plus(any<Duration>()) } doReturn it
            on { toInstant() } doReturn instant
            stubbing(it)
        }
    }
}