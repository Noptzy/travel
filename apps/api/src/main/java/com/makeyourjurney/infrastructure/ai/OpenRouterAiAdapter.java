package com.makeyourjurney.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeyourjurney.domain.enums.TravelMode;
import com.makeyourjurney.domain.enums.TripStyle;
import com.makeyourjurney.domain.model.TripIntent;
import com.makeyourjurney.domain.port.AiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "false")
public class OpenRouterAiAdapter implements AiPort {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterAiAdapter.class);

    private static final String SYSTEM_PROMPT = """
            Kamu adalah asisten AI travel planner Indonesia. Ubah pesan user menjadi JSON intent dengan field:
            origin, destination, startDate (yyyy-MM-dd atau null), endDate (yyyy-MM-dd atau null), days, nights,
            people, budget, tripStyle (HEMAT|BALANCED|NYAMAN|FAMILY|COUPLE|SOLO|HEALING|ADVENTURE atau null),
            travelMode (CAR|MOTOR|WALK|BIKE|PUBLIC atau null), preferences (array string).
            Balas HANYA dengan JSON object tersebut, tanpa teks lain.
            """;

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenRouterAiAdapter(WebClient aiWebClient, ObjectMapper objectMapper, @Value("${app.ai.model}") String model) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public TripIntent parseIntent(String message, TripIntent previousIntent) {
        try {
            JsonNode content = requestIntentJson(message);
            return TripIntentMerger.merge(toTripIntent(content), previousIntent);
        } catch (Exception e) {
            log.warn("OpenRouter intent parse failed, keeping previous intent", e);
            return TripIntentMerger.merge(emptyIntent(), previousIntent);
        }
    }

    private JsonNode requestIntentJson(String message) {
        Map<String, Object> body = Map.of(
                "model", model,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", message)
                )
        );

        JsonNode response = aiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        String content = response.path("choices").path(0).path("message").path("content").asText();
        try {
            return objectMapper.readTree(content);
        } catch (Exception e) {
            throw new IllegalStateException("AI response was not valid JSON", e);
        }
    }

    TripIntent toTripIntent(JsonNode node) {
        return new TripIntent(
                text(node, "origin"),
                text(node, "destination"),
                date(node, "startDate"),
                date(node, "endDate"),
                intValue(node, "days"),
                intValue(node, "nights"),
                intValue(node, "people"),
                decimal(node, "budget"),
                enumValue(node, "tripStyle", TripStyle.class),
                enumValue(node, "travelMode", TravelMode.class),
                stringList(node, "preferences"),
                List.of(),
                false
        );
    }

    private TripIntent emptyIntent() {
        return new TripIntent(null, null, null, null, null, null, null, null, null, null, List.of(), List.of(), false);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private LocalDate date(JsonNode node, String field) {
        String value = text(node, field);
        return value == null ? null : LocalDate.parse(value);
    }

    private Integer intValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : new BigDecimal(value.asText());
    }

    private <E extends Enum<E>> E enumValue(JsonNode node, String field, Class<E> type) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<String> stringList(JsonNode node, String field) {
        List<String> values = new ArrayList<>();
        node.path(field).forEach(item -> values.add(item.asText()));
        return values;
    }
}
