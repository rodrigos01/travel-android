package com.combah.travel2.extensions

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Observable

inline fun <reified T : Any> CollectionReference.asObservable(): Observable<List<T>> {
    return Observable.create { emitter ->
        this.addSnapshotListener { querySnapshot, exception ->
            when {
                querySnapshot != null -> emitter.onNext(querySnapshot.documents.toObjectList())
                exception != null -> emitter.onError(exception)
                else -> emitter.onError(Throwable())
            }
        }
    }
}

inline fun <reified T : Any> List<DocumentSnapshot>.toObjectList(): List<T> {
    return this.mapNotNull {
        it.toObject(T::class.java)
    }
}