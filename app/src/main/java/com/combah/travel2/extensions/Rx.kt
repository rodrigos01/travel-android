package com.combah.travel2.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

fun <T> Observable<T>.asLiveData(strategy: BackpressureStrategy = BackpressureStrategy.LATEST): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(strategy))
}