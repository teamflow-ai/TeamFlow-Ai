package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Assign a bug to an employee")
public record AssignBugRequest(
        @NotNull(message = "Assignee is required")
        UUID assigneeId) {
}
