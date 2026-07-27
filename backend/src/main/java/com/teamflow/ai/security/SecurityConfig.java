package com.teamflow.ai.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Skeleton security configuration.
 * <p>
 * This is a placeholder only — no authentication mechanism (JWT, sessions, OAuth, etc.)
 * has been implemented yet. All endpoints are currently permitted so the foundation
 * project can start and be exercised (e.g. via Swagger UI) before the authentication
 * module is built.
 * <p>
 * This class WILL be replaced/extended in the authentication module.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

}
