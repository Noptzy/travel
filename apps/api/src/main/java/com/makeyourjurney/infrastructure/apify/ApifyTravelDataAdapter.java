package com.makeyourjurney.infrastructure.apify;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.Review;
import com.makeyourjurney.domain.port.CachePort;
import com.makeyourjurney.domain.port.TravelDataPort;
import com.makeyourjurney.infrastructure.maps.NominatimActivityFallbackClient;
import com.makeyourjurney.infrastructure.maps.NominatimHotelFallbackClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.apify.enabled", havingValue = "true")
public class ApifyTravelDataAdapter implements TravelDataPort {

    private final ApifyActorRunner actorRunner;
    private final ApifyNormalizer normalizer;
    private final CachePort cachePort;
    private final NominatimHotelFallbackClient nominatimHotelFallbackClient;
    private final NominatimActivityFallbackClient nominatimActivityFallbackClient;
    private final String hotelActorId;
    private final String activityActorId;
    private final String reviewActorId;
    private final String placesActorId;
    private final Duration cacheTtl;

    public ApifyTravelDataAdapter(
            ApifyActorRunner actorRunner,
            ApifyNormalizer normalizer,
            CachePort cachePort,
            NominatimHotelFallbackClient nominatimHotelFallbackClient,
            NominatimActivityFallbackClient nominatimActivityFallbackClient,
            @Value("${app.apify.hotel-actor-id}") String hotelActorId,
            @Value("${app.apify.activity-actor-id}") String activityActorId,
            @Value("${app.apify.review-actor-id}") String reviewActorId,
            @Value("${app.apify.places-actor-id}") String placesActorId,
            @Value("${app.apify.cache-ttl-hours}") long cacheTtlHours
    ) {
        this.actorRunner = actorRunner;
        this.normalizer = normalizer;
        this.cachePort = cachePort;
        this.nominatimHotelFallbackClient = nominatimHotelFallbackClient;
        this.nominatimActivityFallbackClient = nominatimActivityFallbackClient;
        this.hotelActorId = hotelActorId;
        this.activityActorId = activityActorId;
        this.reviewActorId = reviewActorId;
        this.placesActorId = placesActorId;
        this.cacheTtl = Duration.ofHours(cacheTtlHours);
    }

    @Override
    public List<HotelOption> searchHotels(String destination, LocalDate checkIn, LocalDate checkOut, int people, int limit) {
        String cacheKey = ApifyCacheKeyBuilder.hotelsKey(destination, checkIn, checkOut, people, limit);
        return cachePort.get(cacheKey, HotelOption[].class)
                .map(List::of)
                .orElseGet(() -> {
                    try {
                        String searchLocation = normalizeDestination(destination);
                        JsonNode travelokaItems = actorRunner.runSyncGetDatasetItems(
                                hotelActorId,
                                ApifyActorInputs.hotelSearchInput(hotelActorId, searchLocation, checkIn, checkOut, people, limit)
                        );
                        List<HotelOption> hotels = normalizer.toHotelOptions(travelokaItems).stream()
                                .filter(hotel -> hotel.name() != null && !hotel.name().isBlank())
                                .filter(hotel -> hotel.pricePerNight().compareTo(java.math.BigDecimal.ZERO) > 0)
                                .limit(limit)
                                .toList();
                        if (hotels.isEmpty()) {
                            Object input = ApifyActorInputs.placesInput(searchLocation, "hotel in " + searchLocation, limit);
                            JsonNode items = actorRunner.runSyncGetDatasetItems(placesActorId, input);
                            hotels = normalizer.toGooglePlaceHotels(items).stream()
                                    .filter(hotel -> hotel.name() != null && !hotel.name().isBlank())
                                    .limit(limit)
                                    .toList();
                        }
                        if (hotels.isEmpty()) hotels = nominatimHotelFallbackClient.search(searchLocation, limit);
                        if (!hotels.isEmpty()) cachePort.put(cacheKey, hotels.toArray(HotelOption[]::new), cacheTtl);
                        return hotels;
                    } catch (RuntimeException unavailable) {
                        return nominatimHotelFallbackClient.search(normalizeDestination(destination), limit);
                    }
                });
    }

    @Override
    public List<ActivityOption> searchActivities(String destination, int limit) {
        String cacheKey = ApifyCacheKeyBuilder.activitiesKey(destination, limit);
        return cachePort.get(cacheKey, ActivityOption[].class)
                .map(List::of)
                .orElseGet(() -> {
                    try {
                        String searchLocation = normalizeDestination(destination);
                        Object input = ApifyActorInputs.placesInput(searchLocation, "tourist attraction in " + searchLocation, limit);
                        JsonNode items = actorRunner.runSyncGetDatasetItems(placesActorId, input);
                        List<ActivityOption> activities = normalizer.toGooglePlaceActivities(items).stream().limit(limit).toList();
                        if (activities.isEmpty()) activities = nominatimActivityFallbackClient.search(searchLocation, limit);
                        if (!activities.isEmpty()) cachePort.put(cacheKey, activities.toArray(ActivityOption[]::new), cacheTtl);
                        return activities;
                    } catch (RuntimeException unavailable) {
                        return nominatimActivityFallbackClient.search(normalizeDestination(destination), limit);
                    }
                });
    }

    @Override
    public List<Review> fetchReviews(String sourceUrl, int limit) {
        String cacheKey = ApifyCacheKeyBuilder.reviewsKey(sourceUrl, limit);
        return cachePort.get(cacheKey, Review[].class)
                .map(List::of)
                .orElseGet(() -> {
                    Object input = ApifyActorInputs.reviewInput(sourceUrl, limit);
                    JsonNode items = actorRunner.runSyncGetDatasetItems(reviewActorId, input);
                    List<Review> reviews = normalizer.toReviews(items).stream().limit(limit).toList();
                    cachePort.put(cacheKey, reviews.toArray(Review[]::new), cacheTtl);
                    return reviews;
                });
    }

    private String normalizeDestination(String destination) {
        String normalized = destination.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "amerika", "amerika-serikat", "amerika serikat", "usa", "us" -> "United States";
            case "jepang" -> "Japan";
            case "korea", "korea-selatan", "korea selatan" -> "South Korea";
            case "singapura" -> "Singapore";
            default -> destination;
        };
    }
}
