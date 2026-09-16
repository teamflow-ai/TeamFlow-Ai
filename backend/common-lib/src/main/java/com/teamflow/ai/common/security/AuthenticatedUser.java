package com.teamflow.ai.common.security;

import java.util.List;
import java.util.UUID;

/**
 * Immutable view of the caller, derived from a verified access token and stored as
 * the Spring Security principal.
 *
 * <p>Downstream services read this instead of re-parsing the token, and services
 * read directly rather than re-parsing the token on every call.
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String role,
        List<String> permissions,
        UUID employeeId) {

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean hasRole(String candidate) {
        return role != null && role.equals(candidate);
    }
}
