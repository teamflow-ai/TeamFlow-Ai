package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Create a sprint")
public record CreateSprintRequest(
        @NotNull(message = "Project is required")
        UUID projectId,

        @NotBlank(message = "Sprint name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String goal,

        LocalDate startDate,

        LocalDate endDate) {
}
