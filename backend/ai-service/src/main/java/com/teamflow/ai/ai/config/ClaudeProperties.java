package com.teamflow.ai.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Bound from {@code teamflow.ai.claude.*}. Active only when {@code teamflow.ai.provider=claude}. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "teamflow.ai.claude")
public class ClaudeProperties {

    private String apiKey;

    private String model = "claude-haiku-4-5-20251001";

    private String baseUrl = "https://api.anthropic.com/v1";

    private String apiVersion = "2023-06-01";

    private int timeoutMs = 8000;
}
