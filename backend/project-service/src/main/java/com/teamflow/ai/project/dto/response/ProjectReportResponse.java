package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.ProjectStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Builder
public record ProjectReportResponse(
        UUID projectId,
        String projectName,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        int teamSize,
        long totalTasks,
        Map<String, Long> tasksByStatus,
        long totalBugs,
        long openBugs,
        BigDecimal totalWorklogHours) {
}
