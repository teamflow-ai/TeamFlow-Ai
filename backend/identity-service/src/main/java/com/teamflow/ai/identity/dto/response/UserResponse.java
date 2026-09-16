package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public projection of a user account.
 *
 * <p>Deliberately omits the password hash, lockout counters and audit columns.
 * Entities are never returned directly from a controller, precisely so that adding
 * a sensitive column later cannot silently widen an API response.
 */
@Builder
@Schema(description = "Authenticated user profile")
public record UserResponse(

        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,

        @Schema(example = "PROJECT_MANAGER")
        String role,

        @Schema(description = "Resolved permission names, for frontend route guarding")
        List<String> permissions,

        UUID employeeId,
        String designation,
        String profileImageUrl,
        boolean emailVerified,
        Instant lastLoginAt) {
}
