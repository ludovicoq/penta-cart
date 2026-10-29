package com.pentacart.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.pentacart.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty() && !isLoading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun signIn() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Se va a buon fine cambia la sessione e AppRoot passa da sola alla schermata successiva.
            auth.signIn(current.email, current.password).onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.toMessage()) }
            }
        }
    }

    private fun Throwable.toMessage(): String = when (this) {
        is FirebaseNetworkException -> "Nessuna connessione a internet. Riprova quando c'è campo."
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
            "Email o password non corretti."
        is FirebaseTooManyRequestsException -> "Troppi tentativi. Aspetta qualche minuto e riprova."
        else -> "Accesso non riuscito. Riprova."
    }
}
