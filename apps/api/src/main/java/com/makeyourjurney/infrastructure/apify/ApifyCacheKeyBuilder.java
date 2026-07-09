package com.makeyourjurney.infrastructure.apify;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public final class ApifyCacheKeyBuilder {

    private ApifyCacheKeyBuilder() {
    }

    public static String hotelsKey(String destination, LocalDate checkIn, LocalDate checkOut, int people, int limit) {
        return "apify:hotels:%s:%s:%s:%d:%d".formatted(slugify(destination), checkIn, checkOut, people, limit);
    }

    public static String activitiesKey(String destination, int limit) {
        return "apify:activities:%s:%d".formatted(slugify(destination), limit);
    }

    public static String reviewsKey(String sourceUrl, int limit) {
        return "apify:reviews:%s:%d".formatted(hash(sourceUrl), limit);
    }

    private static String slugify(String city) {
        return city.trim().toLowerCase(Locale.ROOT).replace(" ", "-");
    }

    private static String hash(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
