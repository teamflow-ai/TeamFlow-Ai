package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Project")
public record ProjectResponse(
        UUID id,
        String name,
        String code,
        String description,
        UUID clientId,
        String clientName,
        UUID managerId,
        String managerName,
        ProjectStatus status,
        Priority priority,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        int progressPercent,
        List<ProjectMemberResponse> members) {
}
