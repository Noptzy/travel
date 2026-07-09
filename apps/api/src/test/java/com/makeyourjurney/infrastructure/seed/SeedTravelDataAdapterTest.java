package com.makeyourjurney.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SeedTravelDataAdapterTest {

    private final SeedTravelDataAdapter adapter = new SeedTravelDataAdapter(new ObjectMapper());

    @Test
    void searchHotels_bandung_returnsSeedHotelsWithImagesAndRatings() {
        var hotels = adapter.searchHotels("Bandung", LocalDate.parse("2026-07-16"), LocalDate.parse("2026-07-24"), 4, 3);

        assertThat(hotels).hasSize(3);
        assertThat(hotels)
                .allSatisfy(hotel -> {
                    assertThat(hotel.city()).containsIgnoringCase("Bandung");
                    assertThat(hotel.name()).doesNotContain("Central Stay");
                    assertThat(hotel.pricePerNight()).isPositive();
                    assertThat(hotel.rating()).isPositive();
                    assertThat(hotel.imageUrl()).startsWith("https://");
                });
    }

    @Test
    void searchActivities_bandung_returnsNonZeroRatedActivities() {
        var activities = adapter.searchActivities("Bandung", 20);

        assertThat(activities).hasSize(4);
        assertThat(activities)
                .allSatisfy(activity -> {
                    assertThat(activity.city()).containsIgnoringCase("Bandung");
                    assertThat(activity.rating()).isPositive();
                    assertThat(activity.imageUrl()).startsWith("https://");
                });
    }

    @Test
    void searchHotels_unknownDestination_returnsGenericHotelsInsteadOfEmptyList() {
        var hotels = adapter.searchHotels("Singapore", LocalDate.parse("2026-07-16"), LocalDate.parse("2026-07-24"), 4, 3);

        assertThat(hotels).hasSize(3);
        assertThat(hotels).extracting(hotel -> hotel.city()).containsOnly("Singapore");
        assertThat(hotels).allSatisfy(hotel -> assertThat(hotel.pricePerNight()).isPositive());
    }

    @Test
    void searchActivities_unknownDestination_returnsGenericActivitiesInsteadOfEmptyList() {
        var activities = adapter.searchActivities("Singapore", 20);

        assertThat(activities).hasSize(4);
        assertThat(activities).extracting(activity -> activity.city()).containsOnly("Singapore");
        assertThat(activities).allSatisfy(activity -> assertThat(activity.rating()).isPositive());
    }

    @Test
    void searchActivities_respectsLimit() {
        var activities = adapter.searchActivities("Bandung", 2);

        assertThat(activities).hasSize(2);
    }
}
