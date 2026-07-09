package com.oop.traveloop.data.repository

import com.oop.traveloop.data.remote.PlanRequestDto
import com.oop.traveloop.data.remote.TravelApi
import com.oop.traveloop.domain.model.*
import com.oop.traveloop.domain.repository.PlanInput
import com.oop.traveloop.domain.repository.TravelRepository

class TravelRepositoryImpl(private val api: TravelApi) : TravelRepository {
    override suspend fun createPlan(input: PlanInput): Result<TripPlan> = runCatching {
        api.createPlan(
            PlanRequestDto(
                origin = input.origin.trim(), destination = input.destination.trim(),
                startDate = input.startDate, endDate = input.endDate,
                days = input.days, nights = (input.days - 1).coerceAtLeast(0), people = input.people,
                budget = input.budget, tripStyle = input.style, travelMode = input.mode,
                preferences = input.preferences,
            )
        ).let { dto ->
            TripPlan(
                id = dto.tripId, origin = dto.origin, destination = dto.destination,
                startDate = dto.startDate, endDate = dto.endDate,
                distanceKm = dto.route.distanceKm, durationLabel = dto.route.durationLabel,
                originPoint = RoutePoint(dto.route.origin.lat, dto.route.origin.lng),
                destinationPoint = RoutePoint(dto.route.destination.lat, dto.route.destination.lng),
                routePoints = dto.route.geometry.coordinates.mapNotNull { point ->
                    if (point.size >= 2) RoutePoint(point[1], point[0]) else null
                },
                routeEstimated = dto.route.estimatedOnly,
                budget = Budget(
                    dto.budget.currency,
                    dto.budget.totalBudget, dto.budget.hotelTotal, dto.budget.activityTotal,
                    dto.budget.foodTotal, dto.budget.intercityTransportTotal,
                    dto.budget.localTransportTotal, dto.budget.bufferTotal,
                    dto.budget.estimatedTotal, dto.budget.remainingBudget, dto.budget.status,
                ),
                packages = dto.packages.map { pack ->
                    TravelPackage(
                        pack.type, pack.estimatedTotal, pack.budgetStatus,
                        pack.hotels.firstOrNull()?.let { Hotel(it.name, it.address.orEmpty(), it.pricePerNight, it.rating, it.imageUrl, it.source, it.sourceUrl) },
                        pack.activities.map { Activity(it.name, it.address.orEmpty(), it.pricePerPerson, it.rating, it.durationHours ?: 0.0, it.imageUrl, it.source, it.sourceUrl) },
                        TransportOption(pack.transport.mode, pack.transport.provider, pack.transport.service, pack.transport.description, pack.transport.estimatedCost),
                        pack.transportOptions.map { TransportOption(it.mode, it.provider, it.service, it.description, it.estimatedCost) },
                        Budget(pack.budget.currency, pack.budget.totalBudget, pack.budget.hotelTotal, pack.budget.activityTotal,
                            pack.budget.foodTotal, pack.budget.intercityTransportTotal, pack.budget.localTransportTotal,
                            pack.budget.bufferTotal, pack.budget.estimatedTotal, pack.budget.remainingBudget, pack.budget.status),
                        pack.reason,
                    )
                },
                itinerary = dto.itinerary.map { day ->
                    ItineraryDay(day.day, day.date, day.items.map { ItineraryItem(it.time, it.title, it.description, it.type, it.estimatedCost) })
                },
            )
        }
    }
}
