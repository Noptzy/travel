package com.makeyourjurney.infrastructure.apify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.model.ActivityOption;
import com.makeyourjurney.domain.model.HotelOption;
import com.makeyourjurney.domain.model.Review;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ApifyNormalizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApifyNormalizer normalizer = new ApifyNormalizer();

    @Test
    void toHotelOptions_parsesNormalizedApifyOutput() throws Exception {
        JsonNode items = objectMapper.readTree("""
                [
                  {
                    "externalId": "traveloka_hotel_123",
                    "name": "The Azure Retreat",
                    "city": "Surabaya",
                    "address": "Surabaya Center",
                    "pricePerNight": 450000,
                    "currency": "IDR",
                    "rating": 8.6,
                    "reviewCount": 1200,
                    "imageUrl": "https://example.com/hotel.jpg",
                    "source": "TRAVELOKA_APIFY",
                    "sourceUrl": "https://www.traveloka.com/hotel/example"
                  }
                ]
                """);

        var hotels = normalizer.toHotelOptions(items);

        assertThat(hotels).hasSize(1);
        HotelOption hotel = hotels.get(0);
        assertThat(hotel.externalId()).isEqualTo("traveloka_hotel_123");
        assertThat(hotel.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(450000));
        assertThat(hotel.rating()).isEqualByComparingTo(BigDecimal.valueOf(8.6));
        assertThat(hotel.reviewCount()).isEqualTo(1200);
        assertThat(hotel.score()).isNull();
    }

    @Test
    void toHotelOptions_parsesTravelokaHotelScraperOutput() throws Exception {
        JsonNode items = objectMapper.readTree("""
                [
                  {
                    "name": "Awann Sewu Boutique Hotel & Suite",
                    "location": "Sekayu, Central Semarang",
                    "star_rating": 4,
                    "current_price": 749.433,
                    "source_url": "https://www.traveloka.com/en-id/hotel/example"
                  }
                ]
                """);

        var hotels = normalizer.toHotelOptions(items);

        assertThat(hotels).hasSize(1);
        HotelOption hotel = hotels.get(0);
        assertThat(hotel.name()).isEqualTo("Awann Sewu Boutique Hotel & Suite");
        assertThat(hotel.address()).isEqualTo("Sekayu, Central Semarang");
        assertThat(hotel.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(749433));
        assertThat(hotel.rating()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(hotel.sourceUrl()).isEqualTo("https://www.traveloka.com/en-id/hotel/example");
    }

    @Test
    void toActivityOptions_parsesNormalizedApifyOutput() throws Exception {
        JsonNode items = objectMapper.readTree("""
                [
                  {
                    "externalId": "traveloka_activity_123",
                    "name": "City Tour Surabaya",
                    "city": "Surabaya",
                    "pricePerPerson": 150000,
                    "currency": "IDR",
                    "rating": 4.7,
                    "reviewCount": 300,
                    "durationHours": 3,
                    "imageUrl": "https://example.com/activity.jpg",
                    "tags": ["city tour", "kuliner"],
                    "source": "TRAVELOKA_APIFY",
                    "sourceUrl": "https://www.traveloka.com/activities/example"
                  }
                ]
                """);

        var activities = normalizer.toActivityOptions(items);

        assertThat(activities).hasSize(1);
        ActivityOption activity = activities.get(0);
        assertThat(activity.externalId()).isEqualTo("traveloka_activity_123");
        assertThat(activity.pricePerPerson()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(activity.durationHours()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(activity.tags()).containsExactly("city tour", "kuliner");
        assertThat(activity.address()).isNull();
    }

    @Test
    void toReviews_parsesNormalizedApifyOutput() throws Exception {
        JsonNode items = objectMapper.readTree("""
                [
                  {
                    "targetUrl": "https://www.traveloka.com/hotel/example",
                    "rating": 9.0,
                    "reviewText": "Hotel bersih dan dekat pusat kota.",
                    "reviewerName": "Guest",
                    "createdAt": "2026-07-01T00:00:00Z",
                    "sentiment": "POSITIVE"
                  }
                ]
                """);

        var reviews = normalizer.toReviews(items);

        assertThat(reviews).hasSize(1);
        Review review = reviews.get(0);
        assertThat(review.rating()).isEqualByComparingTo(BigDecimal.valueOf(9.0));
        assertThat(review.sentiment()).isEqualTo("POSITIVE");
        assertThat(review.createdAt()).isEqualTo(java.time.Instant.parse("2026-07-01T00:00:00Z"));
    }
}
