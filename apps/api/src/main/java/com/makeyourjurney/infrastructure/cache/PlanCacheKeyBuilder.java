package com.makeyourjurney.infrastructure.cache;

import com.makeyourjurney.domain.model.TripRequest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PlanCacheKeyBuilder {

    private PlanCacheKeyBuilder() {
    }

    public static String key(TripRequest request) {
        String raw = "%s|%s|%s|%s|%d|%d|%d|%s|%s|%s|%s".formatted(
                request.origin(), request.destination(), request.startDate(), request.endDate(),
                request.days(), request.nights(), request.people(), request.budget(),
                request.tripStyle(), request.travelMode(), String.join(",", request.preferences())
        );
        return "plan:" + UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }
}
