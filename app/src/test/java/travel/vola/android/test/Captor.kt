package travel.vola.android.test

import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddPlanItemState

object Captor {
    inline fun <reified T : Any> capture(block: KArgumentCaptor<T>.() -> Unit): T {
        val captor = argumentCaptor<T>()
        captor.block()
        return captor.lastValue
    }

    fun <T : PendingData, E : AddPlanItemState> AddPlanItemStore<T, E>.getUpdateResult(
        originalData: T,
    ): T = argumentCaptor<(T) -> T> {
        verify(this@getUpdateResult, atLeastOnce()).update(any(), capture())
    }.lastValue(originalData)
}
