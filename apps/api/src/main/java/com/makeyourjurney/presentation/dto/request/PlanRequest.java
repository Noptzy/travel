package com.makeyourjurney.presentation.dto.request;

import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.TripRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PlanRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Positive int days,
        @Min(0) int nights,
        @Positive int people,
        @NotNull @Positive BigDecimal budget,
        @NotNull TripStyle tripStyle,
        @NotNull TravelMode travelMode,
        List<String> preferences
) {
    public TripRequest toDomain() {
        return new TripRequest(
                origin, destination, startDate, endDate, days, nights, people, budget,
                tripStyle, travelMode, preferences == null ? List.of() : preferences
        );
    }
}
