package com.combah.travel2.test

import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.state.AddPlanItemState
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

    fun <T : AddPlanItemStore.AddPlanData, E : AddPlanItemState> getUpdateResult(
        itemStore: AddPlanItemStore<T, E>,
        originalData: T,
    ): T {
        val updater = capture {
            verify(itemStore).update(eq(originalData.id), capture())
        }
        return updater(originalData)
    }
}