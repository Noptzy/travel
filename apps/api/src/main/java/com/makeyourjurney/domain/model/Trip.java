package com.makeyourjurney.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Trip(
        UUID id,
        TripRequest request,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
