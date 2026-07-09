package com.makeyourjurney.application.actor;

import com.makeyourjurney.application.budget.BudgetCalculationService;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.BudgetSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BudgetActor implements Actor<BudgetActor.Input, BudgetSummary> {

    public record Input(
            BigDecimal totalBudget,
            int people,
            int nights,
            int days,
            BigDecimal hotelPricePerNight,
            List<BigDecimal> activityPricesPerPerson,
            double routeDistanceKm,
            TravelMode travelMode
    ) {
    }

    private final BudgetCalculationService budgetCalculationService;

    public BudgetActor(BudgetCalculationService budgetCalculationService) {
        this.budgetCalculationService = budgetCalculationService;
    }

    @Override
    public BudgetSummary run(Input input) {
        return budgetCalculationService.calculate(
                input.totalBudget(), input.people(), input.nights(), input.days(),
                input.hotelPricePerNight(), input.activityPricesPerPerson(),
                input.routeDistanceKm(), input.travelMode()
        );
    }
}
