package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Everything the React client needs to bootstrap a session in one payload:
 * the tokens plus the profile, role and permissions used to build role-based
 * navigation and guard protected routes without a second round trip.
 */
@Builder
@Schema(description = "Issued token pair and the authenticated user's profile")
public record AuthResponse(

        @Schema(description = "Short-lived bearer token for the Authorization header")
        String accessToken,

        @Schema(description = "Long-lived token used against /auth/refresh-token")
        String refreshToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifetime in seconds", example = "900")
        long expiresIn,

        UserResponse user) {
}
