package com.oop.traveloop.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.domain.repository.LoginInput
import com.oop.traveloop.domain.repository.RegisterInput
import com.oop.traveloop.domain.usecase.LoginUseCase
import com.oop.traveloop.domain.usecase.LogoutUseCase
import com.oop.traveloop.domain.usecase.ObserveAuthSessionUseCase
import com.oop.traveloop.domain.usecase.ObserveUserProfileUseCase
import com.oop.traveloop.domain.usecase.RefreshUserProfileUseCase
import com.oop.traveloop.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthAction {
    data class EmailChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data class NameChanged(val value: String) : AuthAction
    data object SubmitLogin : AuthAction
    data object SubmitRegister : AuthAction
    data object ClearError : AuthAction
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val refreshUserProfileUseCase: RefreshUserProfileUseCase,
    observeAuthSessionUseCase: ObserveAuthSessionUseCase,
    observeUserProfileUseCase: ObserveUserProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthFormState())
    val uiState: StateFlow<AuthFormState> = _uiState.asStateFlow()

    val session: StateFlow<AuthSession?> = observeAuthSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val profile: StateFlow<UserProfile?> = observeUserProfileUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            session.collect { authSession ->
                if (authSession != null && profile.value == null) {
                    refreshUserProfileUseCase().onFailure { error ->
                        val text = error.message.orEmpty()
                        if (text.startsWith("HTTP ")) logoutUseCase()
                    }
                }
            }
        }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChanged -> _uiState.update { it.copy(email = action.value, error = null) }
            is AuthAction.PasswordChanged -> _uiState.update { it.copy(password = action.value, error = null) }
            is AuthAction.NameChanged -> _uiState.update { it.copy(name = action.value, error = null) }
            AuthAction.SubmitLogin -> submitLogin()
            AuthAction.SubmitRegister -> submitRegister()
            AuthAction.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    fun logout() = viewModelScope.launch { logoutUseCase() }

    private fun submitLogin() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val form = _uiState.value
        runCatching { loginUseCase(LoginInput(form.email.trim(), form.password)).getOrThrow() }
            .onSuccess { _uiState.update { it.copy(isLoading = false) } }
            .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = error.toLoginMessage()) } }
    }

    private fun submitRegister() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val form = _uiState.value
        runCatching { registerUseCase(RegisterInput(form.email.trim(), form.password, form.name.trim())).getOrThrow() }
            .onSuccess { _uiState.update { it.copy(isLoading = false) } }
            .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = error.toRegisterMessage()) } }
    }

    companion object {
        fun factory(
            login: LoginUseCase,
            register: RegisterUseCase,
            logout: LogoutUseCase,
            observeSession: ObserveAuthSessionUseCase,
            observeProfile: ObserveUserProfileUseCase,
            refreshProfile: RefreshUserProfileUseCase,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(login, register, logout, refreshProfile, observeSession, observeProfile) as T
        }
    }
}

private fun Throwable.toLoginMessage(): String {
    val text = message.orEmpty()
    return when {
        text.contains("HTTP 400") || text.contains("HTTP 401") -> "Email atau password salah"
        text.startsWith("HTTP ") -> "Permintaan gagal. Coba lagi."
        text.isNotBlank() -> text
        else -> "Login gagal"
    }
}

private fun Throwable.toRegisterMessage(): String {
    val text = message.orEmpty()
    return when {
        text.contains("HTTP 409") -> "Email sudah terdaftar"
        text.contains("HTTP 400") -> "Nama, email, atau password belum valid"
        text.contains("HTTP 401") -> "Sesi tidak valid. Coba daftar ulang."
        text.startsWith("HTTP ") -> "Registrasi gagal. Coba lagi."
        text.isNotBlank() -> text
        else -> "Registrasi gagal"
    }
}
