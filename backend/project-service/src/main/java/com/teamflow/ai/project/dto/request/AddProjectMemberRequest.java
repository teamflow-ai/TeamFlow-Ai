package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Add a team member to a project")
public record AddProjectMemberRequest(
        @NotNull(message = "Employee is required")
        UUID employeeId,

        @Size(max = 50)
        String roleOnProject) {
}
