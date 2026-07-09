package com.makeyourjurney.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class OrsGeocodingService {

    private final WebClient mapsWebClient;
    private final String apiKey;

    public OrsGeocodingService(WebClient mapsWebClient, @Value("${app.maps.ors-api-key}") String apiKey) {
        this.mapsWebClient = mapsWebClient;
        this.apiKey = apiKey;
    }

    public JsonNode search(String text, String country) {
        return mapsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geocode/search")
                        .queryParam("text", text)
                        .queryParam("boundary.country", country)
                        .queryParam("size", 1)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, apiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(20));
    }
}
