package travel.vola.android.test

import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor

object Captor {
    inline fun <reified T : Any> capture(block: KArgumentCaptor<T>.() -> Unit): T {
        val captor = argumentCaptor<T>()
        captor.block()
        return captor.lastValue
    }
}
