package com.makeyourjurney.domain.model;

import com.makeyourjurney.domain.enums.BudgetStatus;
import com.makeyourjurney.domain.enums.PackageType;

import java.math.BigDecimal;
import java.util.List;

public record TripPackage(
        PackageType type,
        BigDecimal estimatedTotal,
        BudgetStatus budgetStatus,
        List<HotelOption> hotels,
        List<ActivityOption> activities,
        TransportOption transport,
        List<TransportOption> transportOptions,
        BudgetSummary budget,
        String reason
) {
}
