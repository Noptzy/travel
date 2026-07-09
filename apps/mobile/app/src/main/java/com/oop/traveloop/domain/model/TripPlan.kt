package com.oop.traveloop.domain.model

data class TripPlan(
    val id: String,
    val origin: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val distanceKm: Double,
    val durationLabel: String,
    val originPoint: RoutePoint,
    val destinationPoint: RoutePoint,
    val routePoints: List<RoutePoint>,
    val routeEstimated: Boolean,
    val budget: Budget,
    val packages: List<TravelPackage>,
    val itinerary: List<ItineraryDay>,
)

data class RoutePoint(val latitude: Double, val longitude: Double)

data class Budget(
    val currency: String,
    val total: Double,
    val hotel: Double,
    val activity: Double,
    val food: Double,
    val intercityTransport: Double,
    val localTransport: Double,
    val buffer: Double,
    val estimated: Double,
    val remaining: Double,
    val status: String,
)

data class TravelPackage(
    val type: String,
    val estimatedTotal: Double,
    val status: String,
    val hotel: Hotel?,
    val activities: List<Activity>,
    val transport: TransportOption,
    val transportOptions: List<TransportOption>,
    val budget: Budget,
    val reason: String,
)

data class TransportOption(
    val mode: String,
    val provider: String,
    val service: String,
    val description: String,
    val estimatedCost: Double,
)

data class Hotel(val name: String, val address: String, val price: Double, val rating: Double, val imageUrl: String?, val source: String, val sourceUrl: String?)
data class Activity(val name: String, val address: String, val price: Double, val rating: Double, val durationHours: Double, val imageUrl: String?, val source: String, val sourceUrl: String?)
data class ItineraryDay(val day: Int, val date: String, val items: List<ItineraryItem>)
data class ItineraryItem(val time: String, val title: String, val description: String, val type: String, val cost: Double)
