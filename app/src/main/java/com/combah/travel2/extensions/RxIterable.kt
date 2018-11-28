package com.combah.travel2.extensions

import io.reactivex.Observable
import io.reactivex.rxkotlin.zipWith

inline fun <T> Observable<List<T>>.plusMap(crossinline mapper: (List<T>) -> List<T>): Observable<List<T>> {
    return this.map { it.plus(mapper(it)) }
}

fun <T> Observable<List<T>>.plusConcat(other: Observable<List<T>>): Observable<List<T>> {
    return this.zipWith(other) { first, second ->
        first.plus(second)
    }
}

fun <T> Observable<List<T>>.sortedWith(comparator: Comparator<T>): Observable<List<T>> {
    return this.map { it.sortedWith(comparator) }
}