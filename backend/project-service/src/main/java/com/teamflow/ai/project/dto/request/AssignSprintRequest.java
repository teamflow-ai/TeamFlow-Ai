package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Assign a task to a sprint")
public record AssignSprintRequest(
        @Schema(description = "The ID of the sprint. Can be null to remove from sprint.")
        UUID sprintId
) {
}
