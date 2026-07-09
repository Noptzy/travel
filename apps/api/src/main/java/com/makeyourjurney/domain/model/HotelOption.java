package com.makeyourjurney.domain.model;

import java.math.BigDecimal;

public record HotelOption(
        String externalId,
        String name,
        String city,
        String address,
        BigDecimal pricePerNight,
        String currency,
        BigDecimal rating,
        int reviewCount,
        String imageUrl,
        String source,
        String sourceUrl,
        BigDecimal score
) {
}
