package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "A single grantable capability")
public record PermissionResponse(
        UUID id,
        String name,
        String description,
        String category) {
}
