package travel.vola.android.test

import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object Mocks {
    @Deprecated("use the Time(String) constructor instead")
    fun mockTime(stubbing: KStubbing<ZonedDateTime>.(ZonedDateTime) -> Unit = {}): ZonedDateTime {
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
}
