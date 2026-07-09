package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.repository.AuthRepository

class RefreshUserProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshProfile()
}
