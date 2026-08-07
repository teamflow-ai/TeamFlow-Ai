package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Client")
public record ClientResponse(
        UUID id,
        String name,
        String code,
        String contactPerson,
        String email,
        String phone,
        String country,
        boolean active) {
}
