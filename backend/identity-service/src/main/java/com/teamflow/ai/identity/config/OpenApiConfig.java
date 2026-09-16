package com.teamflow.ai.identity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI document metadata and the bearer-token security scheme.
 *
 * <p>Consolidates what were previously two separate configuration classes
 * ({@code OpenApiConfig} and {@code SwaggerConfig}) doing one job between them.
 * Declaring the scheme here is what puts the "Authorize" button in Swagger UI, so
 * protected endpoints can actually be exercised from the browser.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI identityServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TeamFlow.AI — Identity & Workforce Service")
                        .description("""
                                Authentication, authorization, and the company's people graph \
                                (employees, departments, teams).

                                Obtain a token from `POST /api/v1/auth/login`, then click **Authorize**
                                and paste the `accessToken` value to call protected endpoints.
                                """)
                        .version("v1")
                        .contact(new Contact().name("TeamFlow.AI").email("support@teamflow.ai"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token issued by /api/v1/auth/login")));
    }
}
