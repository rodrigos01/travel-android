package com.combah.travel2.extensions

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
inline fun <reified T : Any> CollectionReference.asFlow(noinline converter: ((DocumentSnapshot) -> T)? = null): Flow<List<T>> {
    return callbackFlow {
        val registration = addSnapshotListener { snapshot, exception ->
            val values = snapshot?.documents?.let { snapshotListToObject(it, converter) }
            when {
                values != null -> trySendBlocking(values)
                exception != null -> throw exception
                else -> throw Throwable()
            }
        }
        awaitClose { registration.remove() }
    }
}

@ExperimentalCoroutinesApi
inline fun <reified T : Any> DocumentReference.asFlow(noinline converter: ((DocumentSnapshot) -> T)? = null): Flow<T> {
    return callbackFlow {
        val registration = addSnapshotListener { snapshot, exception ->
            val value = snapshot?.let { snapshotToObject(it, converter) }
            when {
                value != null -> trySendBlocking(value)
                exception != null -> throw exception
                else -> throw Throwable()
            }
        }
        awaitClose { registration.remove() }
    }
}

inline fun <reified T : Any> snapshotListToObject(
    snapshots: List<DocumentSnapshot>,
    noinline converter: ((DocumentSnapshot) -> T)? = null
): List<T> {
    return snapshots.mapNotNull {
        snapshotToObject(it, converter)
    }
}

inline fun <reified T : Any> snapshotToObject(
    snapshot: DocumentSnapshot,
    noinline converter: ((DocumentSnapshot) -> T)? = null
): T? {
    return if (converter != null) {
        converter(snapshot)
    } else {
        snapshot.toObject(T::class.java)
    }
}