package com.makeyourjurney.infrastructure.apify;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApifyActorInputsTest {

    @Test
    void hotelSearchInput_usesFilterArgsForTravelokaHotelScraper() {
        var input = ApifyActorInputs.hotelSearchInput(
                "hotels-scrapers/traveloka-hotel-scraper",
                "Semarang",
                LocalDate.of(2026, 7, 16),
                LocalDate.of(2026, 7, 24),
                4,
                3
        );

        assertThat(input).containsKeys("filterArgs", "limitPerPage", "totalLimit", "proxyConfiguration");
        assertThat(input).doesNotContainKey("urls");
        assertThat((List<?>) input.get("filterArgs"))
                .singleElement()
                .asString()
                .isEqualTo("https://www.traveloka.com/en-id/hotel/search?spec=16-07-2026.24-07-2026.1.4.HOTEL_GEO.106587.Semarang.2");
    }

    @Test
    void hotelSearchInput_keepsLegacyShapeForStealthModeActor() {
        var input = ApifyActorInputs.hotelSearchInput(
                "stealth_mode/traveloka-hotels-search-scraper",
                "Bali",
                LocalDate.of(2026, 7, 16),
                LocalDate.of(2026, 7, 17),
                2,
                5
        );

        assertThat(input).containsKeys("urls", "ignore_url_failures", "max_items_per_url", "proxy");
        assertThat(input).doesNotContainKey("filterArgs");
        assertThat((List<?>) input.get("urls"))
                .singleElement()
                .asString()
                .contains("HOTEL_GEO.102746.Bali.2");
    }
}
