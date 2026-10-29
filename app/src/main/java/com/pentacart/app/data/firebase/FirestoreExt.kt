package com.pentacart.app.data.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "Firestore"

/**
 * Listener real-time come Flow. Emette anche dalla cache locale, quindi funziona offline.
 * In caso di errore (es. permesso negato dopo il logout) il flow si chiude senza far crashare l'app.
 */
fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Listener interrotto", error)
            close()
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w(TAG, "Listener interrotto su $path", error)
            close()
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registration.remove() }
}

/**
 * Le scritture non vengono attese: Firestore aggiorna subito la cache locale (e quindi la UI)
 * e le invia al server appena c'è rete. Attenderle bloccherebbe l'app al supermercato senza segnale.
 */
fun <T> Task<T>.logFailure(operation: String): Task<T> =
    addOnFailureListener { Log.e(TAG, "Scrittura fallita: $operation", it) }
