package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Transition a project's status")
public record UpdateProjectStatusRequest(
        @NotNull(message = "Status is required")
        ProjectStatus status) {
}
