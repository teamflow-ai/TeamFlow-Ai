package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Create a project")
public record CreateProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Project code is required")
        @Size(max = 30)
        String code,

        @Size(max = 2000)
        String description,

        UUID clientId,

        @NotNull(message = "Project manager is required")
        UUID managerId,

        Priority priority,

        LocalDate startDate,

        LocalDate endDate,

        BigDecimal budget) {
}
