package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.model.GeoPoint;
import com.makeyourjurney.domain.model.RouteSummary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoJsonMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoJsonMapper mapper = new GeoJsonMapper();

    @Test
    void toGeoPoint_parsesOrsGeocodeResponse() throws Exception {
        JsonNode response = objectMapper.readTree("""
                {
                  "features": [
                    {
                      "geometry": { "coordinates": [106.8272, -6.1754] },
                      "properties": { "label": "Jakarta, Indonesia", "confidence": 0.9 }
                    }
                  ]
                }
                """);

        GeoPoint point = mapper.toGeoPoint(response, "Jakarta");

        assertThat(point.label()).isEqualTo("Jakarta, Indonesia");
        assertThat(point.lat()).isEqualTo(-6.1754);
        assertThat(point.lng()).isEqualTo(106.8272);
        assertThat(point.confidence()).isEqualTo(0.9);
    }

    @Test
    void toGeoPoint_throwsWhenNoFeatures() throws Exception {
        JsonNode response = objectMapper.readTree("{ \"features\": [] }");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mapper.toGeoPoint(response, "Nowhere"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toRouteSummary_parsesOrsDirectionsResponse() throws Exception {
        JsonNode response = objectMapper.readTree("""
                {
                  "features": [
                    {
                      "geometry": { "coordinates": [[106.8272, -6.1754], [112.7521, -7.2575]] },
                      "properties": {
                        "summary": { "distance": 791000, "duration": 64260 },
                        "segments": [
                          {
                            "steps": [
                              { "instruction": "Head south", "distance": 1000, "duration": 300 }
                            ]
                          }
                        ]
                      }
                    }
                  ]
                }
                """);
        GeoPoint origin = new GeoPoint("Jakarta", -6.1754, 106.8272, 0.9);
        GeoPoint destination = new GeoPoint("Surabaya", -7.2575, 112.7521, 0.9);

        RouteSummary summary = mapper.toRouteSummary(response, origin, destination, "driving-car");

        assertThat(summary.distanceKm()).isEqualTo(791.0);
        assertThat(summary.durationMinutes()).isEqualTo(1071);
        assertThat(summary.profile()).isEqualTo("driving-car");
        assertThat(summary.steps()).hasSize(1);
        assertThat(summary.steps().get(0).instruction()).isEqualTo("Head south");
        assertThat(summary.geometry().coordinates()).hasSize(2);
        assertThat(summary.estimatedOnly()).isFalse();
    }
}
