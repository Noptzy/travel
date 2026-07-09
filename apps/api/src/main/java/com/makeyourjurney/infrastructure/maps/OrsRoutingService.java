package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OrsRoutingService {

    private final WebClient mapsWebClient;
    private final String apiKey;

    public OrsRoutingService(WebClient mapsWebClient, @Value("${app.maps.ors-api-key}") String apiKey) {
        this.mapsWebClient = mapsWebClient;
        this.apiKey = apiKey;
    }

    public JsonNode getRoute(double originLng, double originLat, double destLng, double destLat, String profile) {
        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(originLng, originLat),
                        List.of(destLng, destLat)
                ),
                "instructions", true
        );

        return mapsWebClient.post()
                .uri("/v2/directions/{profile}/geojson", profile)
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
    }
}
