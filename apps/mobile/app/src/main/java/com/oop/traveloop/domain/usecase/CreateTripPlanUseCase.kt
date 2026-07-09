package com.oop.traveloop.domain.usecase

import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.domain.repository.PlanInput
import com.oop.traveloop.domain.repository.TravelRepository

class CreateTripPlanUseCase(private val repository: TravelRepository) {
    suspend operator fun invoke(input: PlanInput): Result<TripPlan> {
        require(input.origin.isNotBlank()) { "Kota asal wajib diisi" }
        require(input.destination.isNotBlank()) { "Tujuan wajib diisi" }
        require(input.budget > 0) { "Budget harus lebih dari nol" }
        require(input.people > 0 && input.days > 0) { "Durasi dan jumlah orang tidak valid" }
        return repository.createPlan(input)
    }
}
