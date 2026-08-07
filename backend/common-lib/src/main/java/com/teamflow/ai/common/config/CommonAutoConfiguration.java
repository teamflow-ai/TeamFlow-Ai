package com.teamflow.ai.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.ai.common.exception.GlobalExceptionHandler;
import com.teamflow.ai.common.security.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Wires the shared platform beans into every microservice that puts
 * {@code common-lib} on its classpath.
 *
 * <p>Registered via {@code META-INF/spring/...AutoConfiguration.imports} rather than
 * component scanning. Scanning a library package from three different application
 * roots is fragile — it forces each service to widen {@code @SpringBootApplication}
 * and silently picks up anything later added to the library. Auto-configuration
 * keeps the contract explicit and every bean overridable, since each is guarded by
 * {@link ConditionalOnMissingBean}.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value = AuthenticationEntryPoint.class, name = "jakarta.servlet.http.HttpServletRequest")
    public AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new RestAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value = AccessDeniedHandler.class, name = "jakarta.servlet.http.HttpServletRequest")
    public AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
        return new RestAccessDeniedHandler(objectMapper);
    }
}