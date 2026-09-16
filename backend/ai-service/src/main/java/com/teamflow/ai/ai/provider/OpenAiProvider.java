package com.teamflow.ai.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.teamflow.ai.ai.config.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/** OpenAI-backed {@link AiProvider}. See {@link GeminiAiProvider}'s javadoc for the fail-safe contract. */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "teamflow.ai.provider", havingValue = "openai")
public class OpenAiProvider implements AiProvider {

    private final OpenAiProperties properties;
    private final RuleBasedAiProvider fallback;
    private final RestClient restClient;

    public OpenAiProvider(OpenAiProperties properties, RuleBasedAiProvider fallback) {
        this.properties = properties;
        this.fallback = fallback;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()));
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    @Override
    public String explain(String template, Map<String, Object> variables) {
        String rendered = fallback.explain(template, variables);
        if (!isAvailable()) {
            log.debug("OpenAI has no API key configured; using rule-based explanation");
            return rendered;
        }
        try {
            String prompt = "In one or two plain sentences for a project manager, restate this finding without "
                    + "changing any numbers or facts in it: " + rendered;
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "max_tokens", 200,
                    "messages", List.of(Map.of("role", "user", "content", prompt)));

            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String text = response == null ? null
                    : response.path("choices").path(0).path("message").path("content").asText(null);
            if (text == null || text.isBlank()) {
                log.warn("OpenAI returned an empty response; falling back to rule-based explanation");
                return rendered;
            }
            return text.trim();
        } catch (Exception ex) {
            log.warn("OpenAI call failed ({}); falling back to rule-based explanation", ex.getMessage());
            return rendered;
        }
    }
}
