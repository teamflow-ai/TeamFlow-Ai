package com.teamflow.ai.common.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Supplies the value written into {@code created_by} / {@code updated_by}.
 *
 * <p>Resolves to the authenticated user's email, falling back to {@code SYSTEM} for
 * work performed outside a request — scheduled jobs, RabbitMQ consumers and Flyway
 * seed data all persist rows with no security context present.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_AUDITOR = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM_AUDITOR);
        }
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user.email());
        }
        String name = authentication.getName();
        return Optional.of(name == null || name.isBlank() || "anonymousUser".equals(name) ? SYSTEM_AUDITOR : name);
    }
}
