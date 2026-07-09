package com.makeyourjurney.domain.model;

public record RouteStep(
        String instruction,
        double distanceMeters,
        double durationSeconds
) {
}
