package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.SprintStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record SprintReportResponse(
        UUID sprintId,
        String sprintName,
        SprintStatus status,
        long totalTasks,
        Map<String, Long> tasksByStatus,
        long completedTasks,
        BigDecimal totalEstimatedHours,
        BigDecimal totalActualHours) {
}
