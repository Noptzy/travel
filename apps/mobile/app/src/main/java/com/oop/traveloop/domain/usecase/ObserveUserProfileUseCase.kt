package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserProfileUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<UserProfile?> = repository.profile
}
