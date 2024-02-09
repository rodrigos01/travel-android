package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.test.UnconfinedDispatcherTestRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class MutableMapStateFlowTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    @Test
    fun `set should update map`() {
        val subject = MutableMapStateFlow<String, String>()
        assertThat(subject).isEmpty()
        subject["key"] = "value"
        assertThat(subject["key"]).isEqualTo("value")
    }

    @Test
    fun `set should update StateFlow`() = testScope.runTest {
        val subject = MutableMapStateFlow<String, String>()
        var updateValue: Map<String, String>? = null
        val job = launch {
            subject.collect {
                updateValue = it
            }
        }
        assertThat(subject.value).isEmpty()
        assertThat(updateValue).isEmpty()
        subject["key"] = "value"
        assertThat(subject.value["key"]).isEqualTo("value")
        assertThat(updateValue?.get("key")).isEqualTo("value")
        job.cancel()
    }
}