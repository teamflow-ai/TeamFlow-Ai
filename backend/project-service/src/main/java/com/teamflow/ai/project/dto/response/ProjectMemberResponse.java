package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "A team member on a project")
public record ProjectMemberResponse(
        UUID employeeId,
        String employeeName,
        String roleOnProject,
        Instant addedAt) {
}
