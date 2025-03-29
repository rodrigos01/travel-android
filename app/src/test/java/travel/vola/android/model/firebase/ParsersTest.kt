package travel.vola.android.model.firebase

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.extensions.Time

class ParsersTest {

    @Test
    fun `Time to firebase data model should format time as string with timezone`() {
        val original = "2024-06-14T17:05 +0100"
        val time = Time(original)
        val result = time.toFirebaseDataModel()
        assertThat(result).isEqualTo(original)
    }
}