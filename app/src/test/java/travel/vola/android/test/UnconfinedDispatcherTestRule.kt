package travel.vola.android.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedDispatcherTestRule : TestWatcher() {
    val dispatcher = UnconfinedTestDispatcher().also { Dispatchers.setMain(it) }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
