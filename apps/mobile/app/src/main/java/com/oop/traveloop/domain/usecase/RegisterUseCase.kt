package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.repository.AuthRepository
import com.oop.traveloop.domain.repository.RegisterInput

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(input: RegisterInput): Result<Unit> {
        require(input.name.isNotBlank()) { "Nama wajib diisi" }
        require(input.email.isNotBlank()) { "Email wajib diisi" }
        require(input.password.length >= 8) { "Password minimal 8 karakter" }
        return repository.register(input)
    }
}
