package com.makeyourjurney.domain.model;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public record TripPlanResult(
        UUID tripId,
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        RouteSummary route,
        BudgetSummary budget,
        List<TripPackage> packages,
        List<ItineraryDay> itinerary
) {
}
