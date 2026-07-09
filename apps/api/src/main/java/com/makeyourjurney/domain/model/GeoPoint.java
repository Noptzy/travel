package com.makeyourjurney.domain.model;

public record GeoPoint(
        String label,
        double lat,
        double lng,
        Double confidence
) {
}
