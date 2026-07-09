package com.makeyourjurney.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Review(
        String targetUrl,
        BigDecimal rating,
        String reviewText,
        String reviewerName,
        Instant createdAt,
        String sentiment
) {
}
