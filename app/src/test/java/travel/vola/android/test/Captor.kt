package travel.vola.android.test

import com.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import com.vola.android.ui.trip.creation.usecase.PendingData
import com.vola.android.ui.trip.state.AddPlanItemState
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

object Captor {
    inline fun <reified T : Any> capture(block: KArgumentCaptor<T>.() -> Unit): T {
        val captor = argumentCaptor<T>()
        captor.block()
        return captor.lastValue
    }

    fun <T : PendingData, E : AddPlanItemState> getUpdateResult(
        itemStore: AddPlanItemStore<T, E>,
        originalData: T,
    ): T {
        val updater = capture {
            verify(itemStore).update(eq(originalData.id), capture())
        }
        return updater(originalData)
    }
}