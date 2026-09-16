package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Create a task, unassigned by default")
public record CreateTaskRequest(

        @NotNull(message = "Project is required")
        UUID projectId,

        UUID sprintId,

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @Size(max = 4000)
        String description,

        Priority priority,

        LocalDate dueDate,

        @DecimalMin(value = "0.0", inclusive = false, message = "Estimated hours must be positive")
        BigDecimal estimatedHours,

        Set<String> requiredSkills) {
}
