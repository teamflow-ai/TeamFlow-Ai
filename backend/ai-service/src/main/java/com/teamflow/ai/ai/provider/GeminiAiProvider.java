package com.teamflow.ai.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.teamflow.ai.ai.config.GeminiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Google Gemini-backed {@link AiProvider}.
 *
 * <p>Per the AI PHILOSOPHY and FAIL SAFE AI sections of the product brief: this
 * class never lets a missing key, timeout, rate limit or malformed response
 * propagate to the caller. {@link #explain} always returns a usable string —
 * either Gemini's answer, or the same deterministic rendering
 * {@link RuleBasedAiProvider} would have produced. Business logic never needs to
 * know which one happened.
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "teamflow.ai.provider", havingValue = "gemini")
public class GeminiAiProvider implements AiProvider {

    private final GeminiProperties properties;
    private final RuleBasedAiProvider fallback;
    private final RestClient restClient;

    public GeminiAiProvider(GeminiProperties properties, RuleBasedAiProvider fallback) {
        this.properties = properties;
        this.fallback = fallback;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
    }

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    @Override
    public String explain(String template, Map<String, Object> variables) {
        String rendered = fallback.explain(template, variables);
        if (!isAvailable()) {
            log.debug("Gemini has no API key configured; using rule-based explanation");
            return rendered;
        }
        try {
            String prompt = "In one or two plain sentences for a project manager, restate this finding without "
                    + "changing any numbers or facts in it: " + rendered;
            Map<String, Object> body = Map.of(
                    "contents", java.util.List.of(Map.of("parts", java.util.List.of(Map.of("text", prompt)))));

            JsonNode response = restClient.post()
                    .uri("/models/{model}:generateContent?key={key}", properties.getModel(), properties.getApiKey())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String text = response == null ? null
                    : response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(null);
            if (text == null || text.isBlank()) {
                log.warn("Gemini returned an empty response; falling back to rule-based explanation");
                return rendered;
            }
            return text.trim();
        } catch (Exception ex) {
            log.warn("Gemini call failed ({}); falling back to rule-based explanation", ex.getMessage());
            return rendered;
        }
    }
}
