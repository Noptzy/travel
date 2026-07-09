package com.makeyourjurney.infrastructure.apify;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Set;

@Service
public class ApifyActorRunner {

    private static final int MAX_POLL_ATTEMPTS = 30;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(5);
    private static final Set<String> TERMINAL_STATUSES = Set.of("SUCCEEDED", "FAILED", "TIMED-OUT", "ABORTED");

    private final WebClient apifyWebClient;
    private final String token;
    private final Duration syncTimeout;

    public ApifyActorRunner(
            WebClient apifyWebClient,
            @Value("${app.apify.token}") String token,
            @Value("${app.apify.timeout-seconds}") long timeoutSeconds
    ) {
        this.apifyWebClient = apifyWebClient;
        this.token = token;
        this.syncTimeout = Duration.ofSeconds(Math.max(timeoutSeconds, 5));
    }

    public JsonNode runSyncGetDatasetItems(String actorId, Object input) {
        String encodedActorId = actorId.replace("/", "~");
        try {
            return apifyWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/actors/{actorId}/run-sync-get-dataset-items")
                            .queryParam("token", token)
                            .queryParam("format", "json")
                            .queryParam("clean", "true")
                            .build(encodedActorId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(syncTimeout);
        } catch (RuntimeException syncFailure) {
            throw new IllegalStateException("Apify tidak merespons dalam batas waktu", syncFailure);
        }
    }

    private JsonNode runAsyncGetDatasetItems(String encodedActorId, Object input) {
        JsonNode run = apifyWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/actors/{actorId}/runs")
                        .queryParam("token", token)
                        .build(encodedActorId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));

        String runId = run.path("data").path("id").asText();
        String finalStatus = pollUntilFinished(runId);
        if (!"SUCCEEDED".equals(finalStatus)) {
            throw new IllegalStateException("Apify async run did not succeed: runId=" + runId + " status=" + finalStatus);
        }

        JsonNode finishedRun = fetchRunStatus(runId);
        String datasetId = finishedRun.path("data").path("defaultDatasetId").asText();

        return apifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/datasets/{datasetId}/items")
                        .queryParam("token", token)
                        .queryParam("format", "json")
                        .queryParam("clean", "true")
                        .build(datasetId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
    }

    private String pollUntilFinished(String runId) {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            String status = fetchRunStatus(runId).path("data").path("status").asText();
            if (TERMINAL_STATUSES.contains(status)) {
                return status;
            }
            sleep();
        }
        throw new IllegalStateException("Apify async run polling timed out for runId=" + runId);
    }

    private JsonNode fetchRunStatus(String runId) {
        return apifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/actor-runs/{runId}")
                        .queryParam("token", token)
                        .build(runId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
    }

    private void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling Apify run status", e);
        }
    }
}
