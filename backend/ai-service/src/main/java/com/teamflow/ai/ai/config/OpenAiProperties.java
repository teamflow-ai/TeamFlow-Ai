package com.teamflow.ai.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Bound from {@code teamflow.ai.openai.*}. Active only when {@code teamflow.ai.provider=openai}. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "teamflow.ai.openai")
public class OpenAiProperties {

    private String apiKey;

    private String model = "gpt-4o-mini";

    private String baseUrl = "https://api.openai.com/v1";

    private int timeoutMs = 8000;
}
