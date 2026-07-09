package com.makeyourjurney.domain.model;

import java.util.List;

public record RouteGeometry(
        String type,
        List<List<Double>> coordinates
) {
    public static RouteGeometry lineString(List<List<Double>> coordinates) {
        return new RouteGeometry("LineString", coordinates);
    }
}
