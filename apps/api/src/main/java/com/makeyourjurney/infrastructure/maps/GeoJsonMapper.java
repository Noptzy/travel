package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteGeometry;
import com.makeyourjurney.domain.model.RouteStep;
import com.makeyourjurney.domain.model.RouteSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeoJsonMapper {

    public GeoPoint toGeoPoint(JsonNode geocodeResponse, String fallbackLabel) {
        JsonNode features = geocodeResponse.path("features");
        if (!features.isArray() || features.isEmpty()) {
            throw new IllegalArgumentException("No geocode result for: " + fallbackLabel);
        }
        JsonNode first = features.get(0);
        JsonNode coordinates = first.path("geometry").path("coordinates");
        double lng = coordinates.get(0).asDouble();
        double lat = coordinates.get(1).asDouble();
        String label = first.path("properties").path("label").asText(fallbackLabel);
        double confidence = first.path("properties").path("confidence").asDouble(0.5);
        return new GeoPoint(label, lat, lng, confidence);
    }

    public RouteSummary toRouteSummary(JsonNode directionsResponse, GeoPoint origin, GeoPoint destination, String profile) {
        JsonNode feature = directionsResponse.path("features").get(0);
        JsonNode properties = feature.path("properties");
        JsonNode summary = properties.path("summary");
        double distanceKm = summary.path("distance").asDouble() / 1000.0;
        long durationMinutes = Math.round(summary.path("duration").asDouble() / 60.0);

        List<List<Double>> coordinates = new ArrayList<>();
        for (JsonNode point : feature.path("geometry").path("coordinates")) {
            coordinates.add(List.of(point.get(0).asDouble(), point.get(1).asDouble()));
        }

        List<RouteStep> steps = new ArrayList<>();
        for (JsonNode segment : properties.path("segments")) {
            for (JsonNode step : segment.path("steps")) {
                steps.add(new RouteStep(
                        step.path("instruction").asText(""),
                        step.path("distance").asDouble(),
                        step.path("duration").asDouble()
                ));
            }
        }

        return new RouteSummary(
                origin, destination, distanceKm, durationMinutes, durationLabel(durationMinutes),
                profile, RouteGeometry.lineString(coordinates), steps, false
        );
    }

    private String durationLabel(long minutes) {
        long hours = minutes / 60;
        long remaining = minutes % 60;
        return hours > 0 ? hours + "j " + remaining + "m" : remaining + "m";
    }
}
