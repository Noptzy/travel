package com.oop.traveloop.data.remote

data class PlanRequestDto(
    val origin: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val days: Int,
    val nights: Int,
    val people: Int,
    val budget: Long,
    val tripStyle: String,
    val travelMode: String,
    val preferences: List<String>,
)

data class TripPlanDto(
    val tripId: String,
    val origin: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val route: RouteDto,
    val budget: BudgetDto,
    val packages: List<PackageDto>,
    val itinerary: List<ItineraryDayDto>,
)

data class RouteDto(
    val origin: GeoPointDto,
    val destination: GeoPointDto,
    val distanceKm: Double,
    val durationMinutes: Long,
    val durationLabel: String,
    val profile: String,
    val geometry: RouteGeometryDto,
    val estimatedOnly: Boolean,
)

data class GeoPointDto(val label: String, val lat: Double, val lng: Double)
data class RouteGeometryDto(val type: String, val coordinates: List<List<Double>>)

data class BudgetDto(
    val currency: String,
    val totalBudget: Double,
    val hotelTotal: Double,
    val activityTotal: Double,
    val foodTotal: Double,
    val intercityTransportTotal: Double,
    val localTransportTotal: Double,
    val bufferTotal: Double,
    val estimatedTotal: Double,
    val remainingBudget: Double,
    val status: String,
)

data class PackageDto(
    val type: String,
    val estimatedTotal: Double,
    val budgetStatus: String,
    val hotels: List<HotelDto>,
    val activities: List<ActivityDto>,
    val transport: TransportOptionDto,
    val transportOptions: List<TransportOptionDto>,
    val budget: BudgetDto,
    val reason: String,
)

data class TransportOptionDto(
    val mode: String,
    val provider: String,
    val service: String,
    val description: String,
    val estimatedCost: Double,
)

data class HotelDto(
    val name: String,
    val city: String?,
    val address: String?,
    val pricePerNight: Double,
    val rating: Double,
    val reviewCount: Int,
    val score: Double?,
    val imageUrl: String?,
    val source: String,
    val sourceUrl: String?,
)

data class ActivityDto(
    val name: String,
    val address: String?,
    val pricePerPerson: Double,
    val rating: Double,
    val durationHours: Double?,
    val tags: List<String>?,
    val imageUrl: String?,
    val source: String,
    val sourceUrl: String?,
)

data class ItineraryDayDto(val day: Int, val date: String, val items: List<ItineraryItemDto>)
data class ItineraryItemDto(
    val time: String,
    val title: String,
    val description: String,
    val type: String,
    val estimatedCost: Double,
)
