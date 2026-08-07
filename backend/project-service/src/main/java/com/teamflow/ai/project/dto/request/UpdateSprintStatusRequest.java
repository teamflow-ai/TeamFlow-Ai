package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Transition a sprint's status")
public record UpdateSprintStatusRequest(
        @NotNull(message = "Status is required")
        SprintStatus status) {
}
