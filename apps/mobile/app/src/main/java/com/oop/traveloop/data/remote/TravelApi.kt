package com.oop.traveloop.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface TravelApi {
    @POST("planner/plan")
    suspend fun createPlan(@Body request: PlanRequestDto): TripPlanDto
}

