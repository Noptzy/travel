package com.makeyourjurney.domain.model;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TripIntent(
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        Integer days,
        Integer nights,
        Integer people,
        BigDecimal budget,
        TripStyle tripStyle,
        TravelMode travelMode,
        List<String> preferences,
        List<String> missingFields,
        boolean readyToPlan
) {
}
