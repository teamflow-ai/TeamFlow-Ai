package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "A role and its granted permissions")
public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean systemRole,
        List<String> permissions) {
}
