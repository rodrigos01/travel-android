package com.combah.travel2.extensions

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import io.reactivex.Observable

inline fun <reified T : Any> CollectionReference.asObservable(noinline converter: ((DocumentSnapshot) -> T)? = null): Observable<List<T>> {
    return Observable.create { emitter ->
        addSnapshotListener { snapshot, exception ->
            val values = snapshot?.documents?.let { snapshotListToObject(it, converter) }
            when {
                values != null -> emitter.onNext(values)
                exception != null -> emitter.onError(exception)
                else -> emitter.onError(Throwable())
            }
        }
    }
}

inline fun <reified T : Any> DocumentReference.asObservable(noinline converter: ((DocumentSnapshot) -> T)? = null): Observable<T> {
    lateinit var registration: ListenerRegistration
    return Observable.create<T> { emitter ->
        registration = addSnapshotListener { snapshot, exception ->
            val value = snapshot?.let { snapshotToObject(it, converter) }
            when {
                value != null -> emitter.onNext(value)
                exception != null -> emitter.onError(exception)
                else -> emitter.onError(Throwable())
            }
        }
    }.doOnDispose {
        registration.remove()
    }
}

inline fun <reified T : Any> snapshotListToObject(snapshots: List<DocumentSnapshot>, noinline converter: ((DocumentSnapshot) -> T)? = null): List<T> {
    return snapshots.mapNotNull {
        snapshotToObject(it, converter)
    }
}

inline fun <reified T : Any> snapshotToObject(snapshot: DocumentSnapshot, noinline converter: ((DocumentSnapshot) -> T)? = null): T? {
    return if (converter != null) {
        converter(snapshot)
    } else {
        snapshot.toObject(T::class.java)
    }
}