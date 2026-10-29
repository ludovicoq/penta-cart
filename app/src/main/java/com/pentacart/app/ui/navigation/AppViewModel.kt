package com.pentacart.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.pentacart.app.data.model.Session
import com.pentacart.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    val session: StateFlow<Session> = auth.session

    fun setDisplayName(name: String) {
        val uid = auth.currentUid ?: return
        auth.setDisplayName(uid, name)
    }

    fun signOut() = auth.signOut()
}
