package com.teamflow.ai.ai.dto.request;

import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Inputs for an AI/rule-based effort estimate, gathered before a task is created")
public record TaskEstimationRequest(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority,

        @Schema(description = "LOW, MEDIUM or HIGH — the manager's own read on how involved the work is")
        @NotBlank
        String complexity,

        String technology) {
}
