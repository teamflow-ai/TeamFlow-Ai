package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Update a team")
public record UpdateTeamRequest(

        @NotBlank(message = "Team name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        UUID leadEmployeeId) {
}
