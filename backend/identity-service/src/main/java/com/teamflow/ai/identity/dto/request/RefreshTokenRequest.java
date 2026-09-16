package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Exchanges a valid refresh token for a fresh token pair")
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken) {
}
