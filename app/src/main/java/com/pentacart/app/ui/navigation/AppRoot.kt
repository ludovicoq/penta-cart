package com.pentacart.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.data.model.Session
import com.pentacart.app.ui.auth.LoginScreen
import com.pentacart.app.ui.auth.NameSetupScreen
import com.pentacart.app.ui.auth.NotAuthorizedScreen
import com.pentacart.app.ui.components.LoadingScreen

/** Sceglie la schermata in base alla sessione. Le tab (e i loro ViewModel) esistono solo da collegati. */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    when (val current = session) {
        Session.Loading -> LoadingScreen()
        Session.SignedOut -> LoginScreen()
        is Session.NotAuthorized -> NotAuthorizedScreen(uid = current.uid, onSignOut = viewModel::signOut)
        is Session.NeedsName -> NameSetupScreen(onConfirm = viewModel::setDisplayName)
        is Session.Ready -> MainScaffold(
            user = current.user,
            onRename = viewModel::setDisplayName,
            onSignOut = viewModel::signOut,
        )
    }
}
