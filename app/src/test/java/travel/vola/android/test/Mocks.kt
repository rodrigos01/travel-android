package travel.vola.android.test

import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import travel.vola.android.model.data.Time
import java.util.TimeZone

object Mocks {
    fun mockTime(stubbing: KStubbing<Time>.(Time) -> Unit = {}): Time {
        val timeZoneMock: TimeZone = mock()
        return mock {
            on { timeZone } doReturn timeZoneMock
            on { minus(any<Long>()) } doReturn it
            on { plus(any<Long>()) } doReturn it
            on { timeInMillis } doReturn 0L
            stubbing(it)
        }
    }
}