package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.makeyourjurney.domain.model.HotelOption;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class NominatimHotelFallbackClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "makeyour-jurney/1.0 contact: local-dev")
            .build();

    public List<HotelOption> search(String destination, int limit) {
        JsonNode items;
        try {
            items = webClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("q", "hotel " + normalizeLocation(destination))
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", limit)
                            .queryParam("addressdetails", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(20));
        } catch (RuntimeException unavailable) {
            return List.of();
        }

        List<HotelOption> hotels = new ArrayList<>();
        if (items == null || !items.isArray()) return hotels;

        for (JsonNode item : items) {
            String name = text(item, "name");
            String displayName = text(item, "display_name");
            if (name == null || name.isBlank()) name = firstDisplaySegment(displayName);
            if (name == null || name.isBlank()) continue;
            String sourceUrl = "https://www.google.com/maps/search/?api=1&query=" +
                    URLEncoder.encode(name + " " + destination, StandardCharsets.UTF_8);
            hotels.add(new HotelOption(
                    text(item, "osm_type") + ":" + text(item, "osm_id"),
                    name,
                    city(item, destination),
                    displayName,
                    BigDecimal.ZERO,
                    "IDR",
                    BigDecimal.ZERO,
                    0,
                    null,
                    "OpenStreetMap via Nominatim",
                    sourceUrl,
                    null
            ));
        }
        return hotels;
    }

    private String text(JsonNode item, String field) {
        JsonNode value = item.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String city(JsonNode item, String fallback) {
        JsonNode address = item.path("address");
        for (String key : List.of("city", "town", "municipality", "county", "state")) {
            String value = text(address, key);
            if (value != null && !value.isBlank()) return value;
        }
        return fallback;
    }

    private String firstDisplaySegment(String displayName) {
        if (displayName == null) return null;
        int comma = displayName.indexOf(',');
        return comma > 0 ? displayName.substring(0, comma).trim() : displayName.trim();
    }

    private String normalizeLocation(String destination) {
        String normalized = destination.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "amerika", "amerika-serikat", "amerika serikat", "usa", "us", "united states" -> "United States";
            case "jepang", "japan" -> "Japan";
            case "korea", "korea-selatan", "korea selatan", "south korea" -> "South Korea";
            case "singapura", "singapore" -> "Singapore";
            case "bandung", "jakarta", "surabaya", "bali", "denpasar", "yogyakarta", "jogja", "semarang" -> destination + ", Indonesia";
            default -> destination;
        };
    }
}
