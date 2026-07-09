package com.oop.traveloop.domain.repository

import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

data class RegisterInput(val email: String, val password: String, val name: String)
data class LoginInput(val email: String, val password: String)

interface AuthRepository {
    val session: Flow<AuthSession?>
    val profile: Flow<UserProfile?>
    suspend fun register(input: RegisterInput): Result<Unit>
    suspend fun login(input: LoginInput): Result<Unit>
    suspend fun refreshProfile(): Result<Unit>
    suspend fun logout(): Result<Unit>
}
