package com.teamflow.ai.ai.dto.response;

import com.teamflow.ai.common.enums.HealthCategory;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectHealthResponse(
        UUID projectId,
        String projectName,
        double healthScore,
        HealthCategory category,
        long completedTasks,
        long totalTasks,
        long delayedTasks,
        long blockedTasks,
        long openBugs,
        long employeesOnLeave,
        String summary) {
}
