package com.makeyourjurney.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ActivityOption(
        String externalId,
        String name,
        String city,
        String address,
        BigDecimal pricePerPerson,
        String currency,
        BigDecimal rating,
        int reviewCount,
        String imageUrl,
        String source,
        String sourceUrl,
        BigDecimal durationHours,
        List<String> tags,
        BigDecimal score
) {
}
