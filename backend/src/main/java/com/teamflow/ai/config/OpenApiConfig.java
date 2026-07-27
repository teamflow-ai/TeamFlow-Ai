package com.teamflow.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the core OpenAPI document metadata (title, version, contact, license).
 * Swagger UI / SpringDoc specific behavior is configured in {@link SwaggerConfig}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI teamflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TeamFlow AI API")
                        .description("Backend foundation for the TeamFlow AI platform")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("TeamFlow AI")
                                .email("support@teamflow.ai"))
                        .license(new License()
                                .name("Proprietary")));
    }

}
