package com.makeyourjurney.domain.model;

import java.math.BigDecimal;

public record TransportOption(
        String mode,
        String provider,
        String service,
        String description,
        BigDecimal estimatedCost
) {
}
