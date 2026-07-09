package com.oop.traveloop.data.repository

import com.oop.traveloop.data.local.TokenStore
import com.oop.traveloop.data.remote.AuthApi
import com.oop.traveloop.data.remote.LoginApiRequest
import com.oop.traveloop.data.remote.RefreshApiRequest
import com.oop.traveloop.data.remote.RegisterApiRequest
import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.domain.repository.AuthRepository
import com.oop.traveloop.domain.repository.LoginInput
import com.oop.traveloop.domain.repository.RegisterInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {
    override val session: Flow<AuthSession?> = tokenStore.session
    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    override val profile: Flow<UserProfile?> = profileFlow

    override suspend fun register(input: RegisterInput): Result<Unit> = runCatching {
        val response = api.register(RegisterApiRequest(input.email, input.password, input.name))
        tokenStore.save(response.accessToken, response.refreshToken)
        loadProfile(response.accessToken).getOrElse {
            profileFlow.value = UserProfile("", input.email, input.name)
        }
    }

    override suspend fun login(input: LoginInput): Result<Unit> = runCatching {
        val response = api.login(LoginApiRequest(input.email, input.password))
        tokenStore.save(response.accessToken, response.refreshToken)
        loadProfile(response.accessToken).getOrThrow()
    }

    override suspend fun refreshProfile(): Result<Unit> {
        val accessToken = tokenStore.session.first()?.accessToken ?: return Result.failure(IllegalStateException("Sesi tidak ditemukan"))
        return loadProfile(accessToken)
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        val refreshToken = tokenStore.session.first()?.refreshToken
        if (refreshToken != null) api.logout(RefreshApiRequest(refreshToken))
        tokenStore.clear()
        profileFlow.value = null
    }

    private suspend fun loadProfile(accessToken: String): Result<Unit> = runCatching {
        val response = api.me("Bearer $accessToken")
        profileFlow.value = UserProfile(response.id, response.email, response.name)
    }
}
