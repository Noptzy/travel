package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteSummary;
import com.makeyourjurney.domain.port.CachePort;
import com.makeyourjurney.domain.port.MapsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnExpression("'${app.maps.enabled:false}' == 'true' && '${app.maps.provider:openrouteservice}' == 'openrouteservice'")
public class OrsMapsAdapter implements MapsPort {

    private final OrsGeocodingService geocodingService;
    private final OrsRoutingService routingService;
    private final GeoJsonMapper geoJsonMapper;
    private final CachePort cachePort;
    private final String defaultCountry;
    private final Duration cacheTtl;

    public OrsMapsAdapter(
            OrsGeocodingService geocodingService,
            OrsRoutingService routingService,
            GeoJsonMapper geoJsonMapper,
            CachePort cachePort,
            @Value("${app.maps.default-country}") String defaultCountry,
            @Value("${app.maps.cache-ttl-hours}") long cacheTtlHours
    ) {
        this.geocodingService = geocodingService;
        this.routingService = routingService;
        this.geoJsonMapper = geoJsonMapper;
        this.cachePort = cachePort;
        this.defaultCountry = defaultCountry;
        this.cacheTtl = Duration.ofHours(cacheTtlHours);
    }

    @Override
    public GeoPoint geocode(String query) {
        String cacheKey = RouteCacheKeyBuilder.geocodeKey(defaultCountry, query);
        return cachePort.get(cacheKey, GeoPoint.class).orElseGet(() -> {
            JsonNode response = geocodingService.search(query, defaultCountry);
            GeoPoint point = geoJsonMapper.toGeoPoint(response, query);
            cachePort.put(cacheKey, point, cacheTtl);
            return point;
        });
    }

    @Override
    public RouteSummary route(GeoPoint origin, GeoPoint destination, TravelMode travelMode) {
        String profile = orsProfile(travelMode);
        String cacheKey = RouteCacheKeyBuilder.routeKey(origin.lat(), origin.lng(), destination.lat(), destination.lng(), profile);
        return cachePort.get(cacheKey, RouteSummary.class).orElseGet(() -> {
            JsonNode response = routingService.getRoute(origin.lng(), origin.lat(), destination.lng(), destination.lat(), profile);
            RouteSummary summary = geoJsonMapper.toRouteSummary(response, origin, destination, profile);
            cachePort.put(cacheKey, summary, cacheTtl);
            return summary;
        });
    }

    private String orsProfile(TravelMode mode) {
        return switch (mode) {
            case CAR, MOTOR -> "driving-car";
            case WALK -> "foot-walking";
            case BIKE -> "cycling-regular";
            case PUBLIC -> throw new IllegalArgumentException("Rute transportasi umum belum didukung di MVP ini.");
        };
    }
}
