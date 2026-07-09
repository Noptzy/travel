package com.makeyourjurney.domain.model;

import java.math.BigDecimal;

public record ItineraryItem(
        String time,
        String title,
        String description,
        String type,
        BigDecimal estimatedCost
) {
}
