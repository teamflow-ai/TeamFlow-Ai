package com.teamflow.organization.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger UI grouping configuration.
 * Additional groups can be added as more organization-service endpoints are built.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("organization-service")
                .pathsToMatch("/api/**")
                .build();
    }

}
