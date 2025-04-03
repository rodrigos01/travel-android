package travel.vola.android.extensions

import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.ZoneId

class TimeTest {
    @Test
    fun `String to Time should create Time object with correct values and timezone`() {
        val time = Time("2024-05-21T16:50 +0200")
        Assertions.assertThat(time.year).isEqualTo(2024)
        Assertions.assertThat(time.month).isEqualTo(5)
        Assertions.assertThat(time.dayOfMonth).isEqualTo(21)
        Assertions.assertThat(time.hour).isEqualTo(16)
        Assertions.assertThat(time.minute).isEqualTo(50)
        Assertions.assertThat(time.zone).isEqualTo(ZoneId.of("GMT+2"))
    }
}
