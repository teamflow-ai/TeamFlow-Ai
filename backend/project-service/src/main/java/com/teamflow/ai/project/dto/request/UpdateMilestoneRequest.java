package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Update a project milestone's details")
public record UpdateMilestoneRequest(
        @NotBlank(message = "Milestone title is required")
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        LocalDate dueDate) {
}
