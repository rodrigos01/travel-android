package com.combah.travel2.model.firebase

import com.combah.travel2.extensions.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ParsersTest {

    @Test
    fun `Time to firebase data model should format time as string with timezone`() {
        val original = "2024-06-14T17:05 +0100"
        val time = Time(original)
        val result = time.toFirebaseDataModel()
        assertThat(result).isEqualTo(original)
    }
}