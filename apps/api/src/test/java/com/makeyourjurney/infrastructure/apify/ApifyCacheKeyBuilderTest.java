package com.makeyourjurney.infrastructure.apify;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ApifyCacheKeyBuilderTest {

    @Test
    void hotelsKey_includesDestinationDatesAndLimits() {
        assertThat(ApifyCacheKeyBuilder.hotelsKey("Surabaya", LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 12), 2, 20))
                .isEqualTo("apify:hotels:surabaya:2026-07-10:2026-07-12:2:20");
    }

    @Test
    void activitiesKey_includesDestinationAndLimit() {
        assertThat(ApifyCacheKeyBuilder.activitiesKey("Surabaya", 20))
                .isEqualTo("apify:activities:surabaya:20");
    }

    @Test
    void reviewsKey_hashesSourceUrlDeterministically() {
        String key1 = ApifyCacheKeyBuilder.reviewsKey("https://www.traveloka.com/hotel/example", 30);
        String key2 = ApifyCacheKeyBuilder.reviewsKey("https://www.traveloka.com/hotel/example", 30);

        assertThat(key1).isEqualTo(key2).startsWith("apify:reviews:").endsWith(":30");
    }
}
