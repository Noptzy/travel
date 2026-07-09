package com.makeyourjurney.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteGeometry;
import com.makeyourjurney.domain.model.RouteStep;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.port.MapsPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.maps.enabled", havingValue = "false", matchIfMissing = true)
public class HaversineMapsAdapter implements MapsPort {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final ObjectMapper objectMapper;
    private Map<String, double[]> cityCoordinates;

    public HaversineMapsAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public GeoPoint geocode(String query) {
        double[] coords = coordinates().get(slugify(query));
        if (coords == null) {
            throw new IllegalArgumentException("No seed coordinates for city: " + query);
        }
        return new GeoPoint(query, coords[0], coords[1], 1.0);
    }

    @Override
    public RouteSummary route(GeoPoint origin, GeoPoint destination, TravelMode travelMode) {
        double distanceKm = haversineKm(origin.lat(), origin.lng(), destination.lat(), destination.lng());
        double speedKmh = averageSpeedKmh(travelMode);
        long durationMinutes = Math.round((distanceKm / speedKmh) * 60);

        RouteGeometry geometry = RouteGeometry.lineString(List.of(
                List.of(origin.lng(), origin.lat()),
                List.of(destination.lng(), destination.lat())
        ));
        RouteStep step = new RouteStep(
                "Tempuh jarak lurus sekitar " + Math.round(distanceKm) + " km",
                distanceKm * 1000,
                durationMinutes * 60.0
        );

        return new RouteSummary(
                origin, destination, distanceKm, durationMinutes, durationLabel(durationMinutes),
                travelMode.name().toLowerCase(Locale.ROOT), geometry, List.of(step), true
        );
    }

    private double averageSpeedKmh(TravelMode mode) {
        return switch (mode) {
            case CAR -> 40.0;
            case MOTOR -> 35.0;
            case PUBLIC -> 30.0;
            case BIKE -> 15.0;
            case WALK -> 5.0;
        };
    }

    private String durationLabel(long minutes) {
        long hours = minutes / 60;
        long remaining = minutes % 60;
        return hours > 0 ? hours + "j " + remaining + "m" : remaining + "m";
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private Map<String, double[]> coordinates() {
        if (cityCoordinates == null) {
            cityCoordinates = load();
        }
        return cityCoordinates;
    }

    @SuppressWarnings("unchecked")
    private Map<String, double[]> load() {
        try (InputStream in = new ClassPathResource("seed/city-coordinates.json").getInputStream()) {
            Map<String, Map<String, Double>> raw = objectMapper.readValue(in, Map.class);
            Map<String, double[]> loaded = raw.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new double[]{e.getValue().get("lat"), e.getValue().get("lng")}
            ));
            Map<String, double[]> fallback = new java.util.HashMap<>(fallbackCoordinates());
            fallback.putAll(loaded);
            return fallback;
        } catch (RuntimeException | IOException e) {
            return fallbackCoordinates();
        }
    }

    private Map<String, double[]> fallbackCoordinates() {
        return Map.ofEntries(
                Map.entry("jakarta", new double[]{-6.2088, 106.8456}),
                Map.entry("surabaya", new double[]{-7.2575, 112.7521}),
                Map.entry("bali", new double[]{-8.4095, 115.1889}),
                Map.entry("denpasar", new double[]{-8.6705, 115.2126}),
                Map.entry("bandung", new double[]{-6.9175, 107.6191}),
                Map.entry("yogyakarta", new double[]{-7.7956, 110.3695}),
                Map.entry("jepang", new double[]{35.6762, 139.6503}),
                Map.entry("japan", new double[]{35.6762, 139.6503}),
                Map.entry("tokyo", new double[]{35.6762, 139.6503}),
                Map.entry("korea", new double[]{37.5665, 126.9780}),
                Map.entry("korea-selatan", new double[]{37.5665, 126.9780}),
                Map.entry("south-korea", new double[]{37.5665, 126.9780}),
                Map.entry("seoul", new double[]{37.5665, 126.9780}),
                Map.entry("semarang", new double[]{-6.9667, 110.4167})
        );
    }

    private String slugify(String city) {
        return city.trim().toLowerCase(Locale.ROOT).replace(" ", "-");
    }
}
