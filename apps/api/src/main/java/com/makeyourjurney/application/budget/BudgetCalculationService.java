package com.makeyourjurney.application.budget;

import com.makeyourjurney.domain.enums.BudgetStatus;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.BudgetSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BudgetCalculationService {

    private static final BigDecimal FOOD_PER_DAY_PER_PERSON = BigDecimal.valueOf(150_000);
    private static final BigDecimal LOCAL_TRANSPORT_PER_DAY = BigDecimal.valueOf(200_000);
    private static final BigDecimal BUFFER_PERCENT = BigDecimal.valueOf(0.10);
    private static final BigDecimal CAR_COST_PER_KM = BigDecimal.valueOf(2_000);
    private static final BigDecimal MOTOR_COST_PER_KM = BigDecimal.valueOf(700);

    public BudgetSummary calculate(
            BigDecimal totalBudget,
            int people,
            int nights,
            int days,
            BigDecimal hotelPricePerNight,
            List<BigDecimal> activityPricesPerPerson,
            double routeDistanceKm,
            TravelMode travelMode
    ) {
        BigDecimal intercityTransportTotal = BigDecimal.valueOf(routeDistanceKm).multiply(costPerKm(travelMode));
        return calculateWithTransportCost(totalBudget, people, nights, days, hotelPricePerNight,
                activityPricesPerPerson, intercityTransportTotal);
    }

    public BudgetSummary calculateWithTransportCost(
            BigDecimal totalBudget,
            int people,
            int nights,
            int days,
            BigDecimal hotelPricePerNight,
            List<BigDecimal> activityPricesPerPerson,
            BigDecimal intercityTransportTotal
    ) {
        BigDecimal hotelTotal = hotelPricePerNight.multiply(BigDecimal.valueOf(nights));

        BigDecimal activitySum = activityPricesPerPerson.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal activityTotal = activitySum.multiply(BigDecimal.valueOf(people));

        BigDecimal foodTotal = FOOD_PER_DAY_PER_PERSON
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(people));

        BigDecimal localTransportTotal = LOCAL_TRANSPORT_PER_DAY.multiply(BigDecimal.valueOf(days));

        BigDecimal subtotal = hotelTotal.add(activityTotal).add(foodTotal)
                .add(intercityTransportTotal).add(localTransportTotal);

        BigDecimal bufferTotal = subtotal.multiply(BUFFER_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedTotal = subtotal.add(bufferTotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingBudget = totalBudget.subtract(estimatedTotal).setScale(2, RoundingMode.HALF_UP);

        return new BudgetSummary(
                "IDR",
                totalBudget,
                hotelTotal.setScale(2, RoundingMode.HALF_UP),
                activityTotal.setScale(2, RoundingMode.HALF_UP),
                foodTotal.setScale(2, RoundingMode.HALF_UP),
                intercityTransportTotal.setScale(2, RoundingMode.HALF_UP),
                localTransportTotal.setScale(2, RoundingMode.HALF_UP),
                bufferTotal,
                estimatedTotal,
                remainingBudget,
                statusFor(estimatedTotal, totalBudget)
        );
    }

    private BigDecimal costPerKm(TravelMode mode) {
        return switch (mode) {
            case CAR -> CAR_COST_PER_KM;
            case MOTOR -> MOTOR_COST_PER_KM;
            case WALK, BIKE, PUBLIC -> BigDecimal.ZERO;
        };
    }

    private BudgetStatus statusFor(BigDecimal estimatedTotal, BigDecimal totalBudget) {
        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BudgetStatus.OVER_BUDGET;
        }
        BigDecimal ratio = estimatedTotal.divide(totalBudget, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.valueOf(0.85)) <= 0) return BudgetStatus.UNDER_BUDGET;
        if (ratio.compareTo(BigDecimal.ONE) <= 0) return BudgetStatus.WITHIN_BUDGET;
        if (ratio.compareTo(BigDecimal.valueOf(1.15)) <= 0) return BudgetStatus.ALMOST_OVER_BUDGET;
        return BudgetStatus.OVER_BUDGET;
    }
}
