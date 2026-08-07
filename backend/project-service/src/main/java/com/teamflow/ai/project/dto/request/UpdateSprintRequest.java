package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Update a sprint")
public record UpdateSprintRequest(
        @NotBlank(message = "Sprint name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String goal,

        LocalDate startDate,

        LocalDate endDate) {
}
