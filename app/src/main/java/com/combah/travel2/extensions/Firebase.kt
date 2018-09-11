package com.combah.travel2.extensions

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Observable

inline fun <reified T : Any> CollectionReference.asObservable(): Observable<List<T>> {
    return Observable.create { emitter ->
        addSnapshotListener { snapshot, exception ->
            val values = snapshot?.documents?.toObjectList<T>()
            when {
                values != null -> emitter.onNext(values)
                exception != null -> emitter.onError(exception)
                else -> emitter.onError(Throwable())
            }
        }
    }
}

inline fun <reified T : Any> DocumentReference.asObservable(): Observable<T> {
    return Observable.create { emitter ->
        addSnapshotListener { snapshot, exception ->
            val value = snapshot?.toObject(T::class.java)
            when {
                value != null -> emitter.onNext(value)
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