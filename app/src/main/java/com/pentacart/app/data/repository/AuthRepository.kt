package com.pentacart.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pentacart.app.data.firebase.Collections
import com.pentacart.app.data.firebase.Fields
import com.pentacart.app.data.firebase.logFailure
import com.pentacart.app.data.firebase.toUser
import com.pentacart.app.data.model.Session
import com.pentacart.app.data.model.User
import com.pentacart.app.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Solo login: gli account si creano dalla Firebase Console e i loro UID vanno in config/family. */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationScope appScope: CoroutineScope,
) {
    private val users get() = db.collection(Collections.USERS)

    val currentUid: String? get() = auth.currentUser?.uid

    @OptIn(ExperimentalCoroutinesApi::class)
    val session: StateFlow<Session> = observeAuthState()
        .flatMapLatest { uid -> if (uid == null) flowOf(Session.SignedOut) else observeSession(uid) }
        .stateIn(appScope, SharingStarted.Eagerly, Session.Loading)

    /** L'utente collegato e con un nome, oppure null. Serve alle azioni (es. "Aggiunto da"). */
    val currentUser: User? get() = (session.value as? Session.Ready)?.user

    private fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    /**
     * Il profilo in users/{uid} fa anche da controllo di accesso: se le regole negano la lettura,
     * l'UID non è tra i membri della famiglia.
     */
    private fun observeSession(uid: String): Flow<Session> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> {
                    Log.w("Auth", "Lettura profilo fallita", error)
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(Session.NotAuthorized(uid))
                    }
                }
                snapshot != null -> {
                    val user = snapshot.toUser()
                    when {
                        user != null && user.displayName.isNotBlank() -> trySend(Session.Ready(user))
                        // La cache può non avere ancora il profilo: aspetta il server prima di chiedere il nome.
                        snapshot.metadata.isFromCache -> Unit
                        else -> trySend(Session.NeedsName(uid))
                    }
                }
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }

    fun signOut() = auth.signOut()

    fun setDisplayName(uid: String, displayName: String) {
        users.document(uid)
            .set(mapOf(Fields.UID to uid, Fields.DISPLAY_NAME to displayName.trim()))
            .logFailure("setDisplayName")
    }
}
