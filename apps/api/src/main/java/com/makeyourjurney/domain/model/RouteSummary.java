package com.makeyourjurney.domain.model;

import java.util.List;

public record RouteSummary(
        GeoPoint origin,
        GeoPoint destination,
        double distanceKm,
        long durationMinutes,
        String durationLabel,
        String profile,
        RouteGeometry geometry,
        List<RouteStep> steps,
        boolean estimatedOnly
) {
}
