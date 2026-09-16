package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Sprint capacity and workload report")
public record SprintCapacityReport(
        UUID sprintId,
        String sprintName,
        BigDecimal totalEstimatedHours,
        int totalTeamCapacityHours,
        boolean isOverloaded,
        List<MemberCapacity> memberCapacities
) {
    @Builder
    public record MemberCapacity(
            UUID employeeId,
            String employeeName,
            int weeklyCapacity,
            BigDecimal assignedHours,
            boolean isOverloaded
    ) {}
}
