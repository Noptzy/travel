package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteGeometry;
import com.makeyourjurney.domain.model.RouteStep;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.port.CachePort;
import com.makeyourjurney.domain.port.MapsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnExpression("'${app.maps.enabled:false}' == 'true' && '${app.maps.provider:openrouteservice}' == 'osm'")
public class OsmMapsAdapter implements MapsPort {

    private final CachePort cache;
    private final Duration ttl;
    private final WebClient nominatim = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader(HttpHeaders.USER_AGENT, "makeYour-Jurney/1.0 (student travel planner)")
            .build();
    private final WebClient osrm = WebClient.builder()
            .baseUrl("https://router.project-osrm.org")
            .defaultHeader(HttpHeaders.USER_AGENT, "makeYour-Jurney/1.0")
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(8 * 1024 * 1024)).build())
            .build();

    public OsmMapsAdapter(CachePort cache, @Value("${app.maps.cache-ttl-hours}") long ttlHours) {
        this.cache = cache;
        this.ttl = Duration.ofHours(ttlHours);
    }

    @Override
    public GeoPoint geocode(String query) {
        String key = RouteCacheKeyBuilder.geocodeKey("ID", query);
        return cache.get(key, GeoPoint.class).orElseGet(() -> {
            String resolvedQuery = normalizeLocationQuery(query);
            JsonNode result = nominatim.get().uri(uri -> uri.path("/search")
                    .queryParam("q", resolvedQuery)
                    .queryParam("format", "jsonv2").queryParam("limit", 1).build())
                    .retrieve().bodyToMono(JsonNode.class)
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .block(Duration.ofSeconds(30));
            if (result == null || !result.isArray() || result.isEmpty()) {
                throw new IllegalArgumentException("Lokasi tidak ditemukan: " + query);
            }
            JsonNode item = result.get(0);
            GeoPoint point = new GeoPoint(item.path("display_name").asText(query),
                    item.path("lat").asDouble(), item.path("lon").asDouble(), 1.0);
            cache.put(key, point, ttl);
            return point;
        });
    }

    @Override
    public RouteSummary route(GeoPoint origin, GeoPoint destination, TravelMode travelMode) {
        String profile = travelMode.name().toLowerCase(Locale.ROOT);
        String key = RouteCacheKeyBuilder.routeKey(origin.lat(), origin.lng(), destination.lat(), destination.lng(), "osm-" + profile);
        return cache.get(key, RouteSummary.class).orElseGet(() -> {
            String coordinates = origin.lng() + "," + origin.lat() + ";" + destination.lng() + "," + destination.lat();
            JsonNode response;
            try {
                response = osrm.get().uri(uri -> uri.path("/route/v1/driving/{coordinates}")
                        .queryParam("overview", "full").queryParam("geometries", "geojson")
                        .queryParam("steps", "true").build(coordinates))
                        .retrieve().bodyToMono(JsonNode.class).block(Duration.ofSeconds(30));
            } catch (RuntimeException unavailableRoadRoute) {
                return internationalEstimate(origin, destination, profile);
            }
            JsonNode routes = response == null ? null : response.path("routes");
            if (routes == null || !routes.isArray() || routes.isEmpty()) {
                return internationalEstimate(origin, destination, profile);
            }
            JsonNode route = routes.path(0);
            List<List<Double>> rawPoints = new ArrayList<>();
            route.path("geometry").path("coordinates").forEach(p -> rawPoints.add(List.of(p.get(0).asDouble(), p.get(1).asDouble())));
            List<List<Double>> points = downsample(rawPoints, 800);
            List<RouteStep> steps = new ArrayList<>();
            route.path("legs").path(0).path("steps").forEach(step -> steps.add(new RouteStep(
                    step.path("maneuver").path("instruction").asText(step.path("name").asText("Lanjut")),
                    step.path("distance").asDouble(), step.path("duration").asDouble())));
            double distanceKm = route.path("distance").asDouble() / 1000.0;
            long durationMinutes = Math.round(route.path("duration").asDouble() / 60.0);
            RouteSummary summary = new RouteSummary(origin, destination, distanceKm, durationMinutes,
                    durationMinutes / 60 + "j " + durationMinutes % 60 + "m", profile,
                    RouteGeometry.lineString(points), List.copyOf(steps), false);
            cache.put(key, summary, ttl);
            return summary;
        });
    }

    private List<List<Double>> downsample(List<List<Double>> points, int maxPoints) {
        if (points.size() <= maxPoints) return points;
        List<List<Double>> sampled = new ArrayList<>(maxPoints);
        double step = (points.size() - 1.0) / (maxPoints - 1.0);
        for (int i = 0; i < maxPoints; i++) sampled.add(points.get((int) Math.round(i * step)));
        return sampled;
    }

    private String normalizeLocationQuery(String query) {
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "jepang" -> "Japan";
            case "korea", "korea selatan" -> "South Korea";
            case "belanda" -> "Netherlands";
            case "inggris" -> "United Kingdom";
            case "bandung", "jakarta", "surabaya", "bali", "denpasar", "yogyakarta", "jogja", "semarang" -> query + ", Indonesia";
            default -> query;
        };
    }

    private RouteSummary internationalEstimate(GeoPoint origin, GeoPoint destination, String profile) {
        double distanceKm = haversineKm(origin.lat(), origin.lng(), destination.lat(), destination.lng());
        long durationMinutes = Math.round(distanceKm / 800.0 * 60.0);
        List<List<Double>> line = List.of(
                List.of(origin.lng(), origin.lat()),
                List.of(destination.lng(), destination.lat())
        );
        return new RouteSummary(origin, destination, distanceKm, durationMinutes,
                durationMinutes / 60 + "j " + durationMinutes % 60 + "m (estimasi udara)",
                "air-estimate", RouteGeometry.lineString(line),
                List.of(new RouteStep("Rute lintas negara memerlukan transportasi udara", distanceKm * 1000, durationMinutes * 60)), true);
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double radius = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
