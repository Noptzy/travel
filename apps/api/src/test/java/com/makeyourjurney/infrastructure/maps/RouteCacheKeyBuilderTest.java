package com.makeyourjurney.infrastructure.maps;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouteCacheKeyBuilderTest {

    @Test
    void geocodeKey_lowercasesCountryAndQuery() {
        assertThat(RouteCacheKeyBuilder.geocodeKey("ID", "  Jakarta "))
                .isEqualTo("maps:geocode:id:jakarta");
    }

    @Test
    void routeKey_includesCoordinatesAndProfile() {
        assertThat(RouteCacheKeyBuilder.routeKey(-6.2088, 106.8456, -7.2575, 112.7521, "driving-car"))
                .isEqualTo("maps:route:-6.2088:106.8456:-7.2575:112.7521:driving-car");
    }
}
