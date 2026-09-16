package com.teamflow.ai.project.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the caller's bearer token onto outbound Feign calls.
 *
 * <p>identity-service re-verifies every token itself rather than trusting the
 * gateway (see its {@code SecurityConfig}), so a Feign call carrying no
 * {@code Authorization} header would be rejected with 401. Propagating the
 * original caller's token — rather than minting a service credential — keeps the
 * downstream call authorized as the same principal who made the original request,
 * which is also what makes its {@code @PreAuthorize} checks meaningful.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authorizationForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        };
    }
}
