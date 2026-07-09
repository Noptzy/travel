package com.oop.traveloop.domain.repository

import com.oop.traveloop.domain.model.TripPlan

data class PlanInput(
    val origin: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val days: Int,
    val people: Int,
    val budget: Long,
    val style: String,
    val mode: String,
    val preferences: List<String>,
)

interface TravelRepository {
    suspend fun createPlan(input: PlanInput): Result<TripPlan>
}

