package com.teamflow.ai.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger UI grouping configuration.
 * Additional groups (e.g. "auth", "projects") can be added as future modules are built.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("teamflow-ai")
                .pathsToMatch("/api/**")
                .build();
    }

}
