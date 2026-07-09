package com.oop.traveloop.ui.auth

import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.domain.repository.AuthRepository
import com.oop.traveloop.domain.repository.LoginInput
import com.oop.traveloop.domain.repository.RegisterInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    private val loginError: Throwable? = null,
    private val registerError: Throwable? = null,
) : AuthRepository {
    private val sessionFlow = MutableStateFlow<AuthSession?>(null)
    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    override val session: Flow<AuthSession?> = sessionFlow
    override val profile: Flow<UserProfile?> = profileFlow

    var logoutCalled = false
        private set

    fun setSession(value: AuthSession) {
        sessionFlow.value = value
    }

    fun clearSession() {
        sessionFlow.value = null
    }

    override suspend fun register(input: RegisterInput): Result<Unit> {
        if (registerError != null) return Result.failure(registerError)
        sessionFlow.value = AuthSession("access", "refresh")
        profileFlow.value = UserProfile("id", input.email, input.name)
        return Result.success(Unit)
    }

    override suspend fun login(input: LoginInput): Result<Unit> {
        if (loginError != null) return Result.failure(loginError)
        sessionFlow.value = AuthSession("access", "refresh")
        profileFlow.value = UserProfile("id", input.email, "User Test")
        return Result.success(Unit)
    }

    override suspend fun refreshProfile(): Result<Unit> {
        profileFlow.value = UserProfile("id", "user@example.com", "User Test")
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        logoutCalled = true
        sessionFlow.value = null
        profileFlow.value = null
        return Result.success(Unit)
    }
}
