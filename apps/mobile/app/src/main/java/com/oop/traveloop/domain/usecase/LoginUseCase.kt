package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.repository.AuthRepository
import com.oop.traveloop.domain.repository.LoginInput

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(input: LoginInput): Result<Unit> {
        require(input.email.isNotBlank()) { "Email wajib diisi" }
        require(input.password.isNotBlank()) { "Password wajib diisi" }
        return repository.login(input)
    }
}
