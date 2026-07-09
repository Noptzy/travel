package com.makeyourjurney.application.budget;

import com.makeyourjurney.domain.enums.BudgetStatus;
import com.makeyourjurney.domain.enums.TravelMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetCalculationServiceTest {

    private final BudgetCalculationService service = new BudgetCalculationService();

    @Test
    void calculate_jakartaToSurabayaExample_returnsExpectedBreakdown() {
        var result = service.calculate(
                BigDecimal.valueOf(3_000_000), 2, 2, 3,
                BigDecimal.valueOf(450_000), List.of(BigDecimal.valueOf(150_000)),
                100.0, TravelMode.CAR
        );

        assertThat(result.hotelTotal()).isEqualByComparingTo("900000");
        assertThat(result.activityTotal()).isEqualByComparingTo("300000");
        assertThat(result.foodTotal()).isEqualByComparingTo("900000");
        assertThat(result.intercityTransportTotal()).isEqualByComparingTo("200000");
        assertThat(result.localTransportTotal()).isEqualByComparingTo("600000");
        assertThat(result.bufferTotal()).isEqualByComparingTo("290000");
        assertThat(result.estimatedTotal()).isEqualByComparingTo("3190000");
        assertThat(result.remainingBudget()).isEqualByComparingTo("-190000");
        assertThat(result.status()).isEqualTo(BudgetStatus.ALMOST_OVER_BUDGET);
    }

    @ParameterizedTest
    @CsvSource({
            "4000000, UNDER_BUDGET",
            "3190000, WITHIN_BUDGET",
            "3000000, ALMOST_OVER_BUDGET",
            "2000000, OVER_BUDGET"
    })
    void calculate_statusFollowsBudgetRatioThresholds(BigDecimal totalBudget, BudgetStatus expectedStatus) {
        var result = service.calculate(
                totalBudget, 2, 2, 3,
                BigDecimal.valueOf(450_000), List.of(BigDecimal.valueOf(150_000)),
                100.0, TravelMode.CAR
        );

        assertThat(result.status()).isEqualTo(expectedStatus);
    }
}
