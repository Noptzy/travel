package com.makeyourjurney.infrastructure.maps;

import java.util.Locale;

public final class RouteCacheKeyBuilder {

    private RouteCacheKeyBuilder() {
    }

    public static String geocodeKey(String country, String query) {
        return "maps:geocode:" + country.toLowerCase(Locale.ROOT) + ":" + query.trim().toLowerCase(Locale.ROOT);
    }

    public static String routeKey(double originLat, double originLng, double destLat, double destLng, String profile) {
        return "maps:route:%s:%s:%s:%s:%s".formatted(originLat, originLng, destLat, destLng, profile);
    }
}
