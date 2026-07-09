package com.makeyourjurney.domain.model;

import com.makeyourjurney.domain.enums.BudgetStatus;

import java.math.BigDecimal;

public record BudgetSummary(
        String currency,
        BigDecimal totalBudget,
        BigDecimal hotelTotal,
        BigDecimal activityTotal,
        BigDecimal foodTotal,
        BigDecimal intercityTransportTotal,
        BigDecimal localTransportTotal,
        BigDecimal bufferTotal,
        BigDecimal estimatedTotal,
        BigDecimal remainingBudget,
        BudgetStatus status
) {
}
