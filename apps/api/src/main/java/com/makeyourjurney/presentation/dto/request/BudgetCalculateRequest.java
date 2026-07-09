package com.makeyourjurney.presentation.dto.request;

import com.makeyourjurney.application.actor.BudgetActor;
import com.makeyourjurney.domain.enums.TravelMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record BudgetCalculateRequest(
        @NotNull @Positive BigDecimal budget,
        @Positive int people,
        @Min(0) int nights,
        @Positive int days,
        @NotNull @PositiveOrZero BigDecimal hotelPricePerNight,
        @NotEmpty List<@PositiveOrZero BigDecimal> activityPricesPerPerson,
        @PositiveOrZero double routeDistanceKm,
        @NotNull TravelMode travelMode
) {
    public BudgetActor.Input toActorInput() {
        return new BudgetActor.Input(
                budget, people, nights, days, hotelPricePerNight,
                activityPricesPerPerson, routeDistanceKm, travelMode
        );
    }
}
