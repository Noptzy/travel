package com.makeyourjurney.infrastructure.apify;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.Review;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ApifyNormalizer {

    public List<HotelOption> toGooglePlaceHotels(JsonNode items) {
        List<HotelOption> hotels = new ArrayList<>();
        for (JsonNode item : items) {
            hotels.add(new HotelOption(
                    firstText(item, "placeId", "cid"), firstText(item, "title", "name"),
                    firstText(item, "city", "state"), firstText(item, "address"),
                    BigDecimal.ZERO, "IDR", firstDecimal(item, "totalScore", "rating", "stars"),
                    firstInt(item, "reviewsCount", "reviews", "userRatingsTotal"),
                    firstText(item, "imageUrl", "imageUrls.0", "imageUrls.0.url", "images.0", "images.0.url"),
                    "Google Maps via Apify", firstText(item, "url", "website"), null
            ));
        }
        return hotels;
    }

    public List<ActivityOption> toGooglePlaceActivities(JsonNode items) {
        List<ActivityOption> activities = new ArrayList<>();
        for (JsonNode item : items) {
            List<String> tags = new ArrayList<>();
            item.path("categories").forEach(tag -> tags.add(tag.asText()));
            activities.add(new ActivityOption(
                    firstText(item, "placeId", "cid"), firstText(item, "title", "name"),
                    firstText(item, "city", "state"), firstText(item, "address"),
                    BigDecimal.ZERO, "IDR", firstDecimal(item, "totalScore", "rating", "stars"),
                    firstInt(item, "reviewsCount", "reviews", "userRatingsTotal"),
                    firstText(item, "imageUrl", "imageUrls.0", "imageUrls.0.url", "images.0", "images.0.url"),
                    "Google Maps via Apify", firstText(item, "url", "website"),
                    BigDecimal.ZERO, List.copyOf(tags), null
            ));
        }
        return activities;
    }

    public List<HotelOption> toHotelOptions(JsonNode items) {
        List<HotelOption> hotels = new ArrayList<>();
        for (JsonNode item : items) {
            hotels.add(new HotelOption(
                    firstText(item, "externalId", "hotel_id", "id"),
                    firstText(item, "name", "hotel_name", "property_name"),
                    firstText(item, "city", "city_name", "location.city"),
                    firstText(item, "address", "displayed_location", "location.address", "location"),
                    firstPrice(item, "pricePerNight", "price", "display_price.amount", "rate_info.base_price", "current_price"),
                    defaultText(item, "IDR", "currency", "display_price.currency", "rate_info.currency"),
                    firstDecimal(item, "rating", "score", "review_score", "reviewScore", "reviews.score", "reviews.rating", "starRating", "star_rating"),
                    firstInt(item, "reviewCount", "total_review", "review_count", "reviews.count", "reviews.total", "reviews"),
                    firstText(item, "imageUrl", "image_url", "thumbnail_url", "imageUrls.0", "imageUrls.0.url", "images.0", "images.0.url"),
                    "Traveloka via Apify",
                    firstText(item, "sourceUrl", "from_url", "url", "source_url"),
                    null
            ));
        }
        return hotels;
    }

    public List<ActivityOption> toActivityOptions(JsonNode items) {
        List<ActivityOption> activities = new ArrayList<>();
        for (JsonNode item : items) {
            List<String> tags = new ArrayList<>();
            item.path("tags").forEach(tag -> tags.add(tag.asText()));
            activities.add(new ActivityOption(
                    firstText(item, "externalId", "experience_id", "id"),
                    firstText(item, "name", "experience_name", "experience_name_en"),
                    firstText(item, "city", "city_name", "short_geo_name"),
                    firstText(item, "address", "displayed_location", "short_geo_name"),
                    firstDecimal(item, "pricePerPerson", "base_price.discounted_price.currency_value.amount", "base_price.original_price.currency_value.amount"),
                    defaultText(item, "IDR", "currency", "base_price.discounted_price.currency_value.currency", "base_price.original_price.currency_value.currency"),
                    firstDecimal(item, "rating", "score", "review_score", "reviewScore", "totalScore", "reviews.score", "reviews.rating", "averageRating"),
                    firstInt(item, "reviewCount", "total_review", "review_count", "reviews.count", "reviews.total"),
                    firstText(item, "imageUrl", "image_url", "thumbnail_url", "image_urls.0", "imageUrls.0", "imageUrls.0.url", "images.0", "images.0.url"),
                    "Traveloka via Apify",
                    firstText(item, "sourceUrl", "from_url", "url"),
                    firstDecimal(item, "durationHours", "duration_hours"),
                    List.copyOf(tags),
                    null
            ));
        }
        return activities;
    }

    public List<Review> toReviews(JsonNode items) {
        List<Review> reviews = new ArrayList<>();
        for (JsonNode item : items) {
            reviews.add(new Review(
                    text(item, "targetUrl"),
                    decimal(item, "rating"),
                    text(item, "reviewText"),
                    text(item, "reviewerName"),
                    instant(item, "createdAt"),
                    text(item, "sentiment")
            ));
        }
        return reviews;
    }

    private String text(JsonNode item, String field) {
        JsonNode value = item.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode item, String field) {
        JsonNode value = item.path(field);
        return value.isMissingNode() || value.isNull() ? null : new BigDecimal(value.asText());
    }

    private JsonNode at(JsonNode item, String path) {
        JsonNode node = item;
        for (String part : path.split("\\.")) {
            node = part.matches("\\d+") ? node.path(Integer.parseInt(part)) : node.path(part);
        }
        return node;
    }

    private String firstText(JsonNode item, String... paths) {
        for (String path : paths) {
            JsonNode value = at(item, path);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) return value.asText();
        }
        return null;
    }

    private String defaultText(JsonNode item, String fallback, String... paths) {
        String value = firstText(item, paths);
        return value == null ? fallback : value;
    }

    private BigDecimal firstDecimal(JsonNode item, String... paths) {
        String value = firstText(item, paths);
        if (value == null) return BigDecimal.ZERO;
        try { return new BigDecimal(value.replaceAll("[^0-9.-]", "")); }
        catch (NumberFormatException ignored) { return BigDecimal.ZERO; }
    }

    private BigDecimal firstPrice(JsonNode item, String... paths) {
        String value = firstText(item, paths);
        if (value == null) return BigDecimal.ZERO;
        String normalized = value.trim().replaceAll("[^0-9,.-]", "");
        if (normalized.isBlank()) return BigDecimal.ZERO;
        boolean hasDot = normalized.contains(".");
        boolean hasComma = normalized.contains(",");
        if (hasDot && hasComma) {
            normalized = normalized.lastIndexOf(',') > normalized.lastIndexOf('.')
                    ? normalized.replace(".", "").replace(',', '.')
                    : normalized.replace(",", "");
        } else if (hasDot && normalized.matches("-?\\d{1,3}(\\.\\d{3})+")) {
            normalized = normalized.replace(".", "");
        } else if (hasComma && normalized.matches("-?\\d{1,3}(,\\d{3})+")) {
            normalized = normalized.replace(",", "");
        } else if (hasComma) {
            normalized = normalized.replace(',', '.');
        }
        try { return new BigDecimal(normalized); }
        catch (NumberFormatException ignored) { return BigDecimal.ZERO; }
    }

    private int firstInt(JsonNode item, String... paths) {
        return firstDecimal(item, paths).intValue();
    }

    private Instant instant(JsonNode item, String field) {
        JsonNode value = item.path(field);
        return value.isMissingNode() || value.isNull() ? null : Instant.parse(value.asText());
    }
}
