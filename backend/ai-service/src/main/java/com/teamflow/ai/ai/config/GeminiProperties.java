package com.teamflow.ai.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Bound from {@code teamflow.ai.gemini.*}. Active only when {@code teamflow.ai.provider=gemini}. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "teamflow.ai.gemini")
public class GeminiProperties {

    /** Never logged, never sent anywhere except Google's endpoint — see AI SECURITY in the product brief. */
    private String apiKey;

    private String model = "gemini-2.0-flash";

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    private int timeoutMs = 8000;
}
