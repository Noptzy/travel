package com.makeyourjurney.domain.model;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TripRequest(
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        int days,
        int nights,
        int people,
        BigDecimal budget,
        TripStyle tripStyle,
        TravelMode travelMode,
        List<String> preferences
) {
}
