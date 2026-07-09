package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<AuthSession?> = repository.session
}
