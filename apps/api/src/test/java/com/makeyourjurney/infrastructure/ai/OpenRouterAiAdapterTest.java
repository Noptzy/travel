package com.makeyourjurney.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.TripIntent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterAiAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenRouterAiAdapter adapter = new OpenRouterAiAdapter(null, objectMapper, "test-model");

    @Test
    void toTripIntent_parsesSpecIntentExtractionShape() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "origin": "Jakarta",
                  "destination": "Surabaya",
                  "startDate": null,
                  "endDate": null,
                  "days": 3,
                  "nights": 2,
                  "people": 2,
                  "budget": 3000000,
                  "tripStyle": "HEMAT",
                  "travelMode": "CAR",
                  "preferences": ["hemat", "nyaman"]
                }
                """);

        TripIntent intent = adapter.toTripIntent(node);

        assertThat(intent.origin()).isEqualTo("Jakarta");
        assertThat(intent.destination()).isEqualTo("Surabaya");
        assertThat(intent.startDate()).isNull();
        assertThat(intent.budget()).isEqualByComparingTo(BigDecimal.valueOf(3_000_000));
        assertThat(intent.tripStyle()).isEqualTo(TripStyle.HEMAT);
        assertThat(intent.travelMode()).isEqualTo(TravelMode.CAR);
        assertThat(intent.preferences()).containsExactly("hemat", "nyaman");
    }

    @Test
    void toTripIntent_parsesDatesAndUnknownEnumAsNull() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "destination": "Bali",
                  "startDate": "2026-07-10",
                  "endDate": "2026-07-12",
                  "tripStyle": "NOT_A_REAL_STYLE"
                }
                """);

        TripIntent intent = adapter.toTripIntent(node);

        assertThat(intent.startDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(intent.endDate()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(intent.tripStyle()).isNull();
        assertThat(intent.preferences()).isEmpty();
    }
}
