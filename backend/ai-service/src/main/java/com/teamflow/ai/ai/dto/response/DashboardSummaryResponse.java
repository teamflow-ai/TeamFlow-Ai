package com.teamflow.ai.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Company-wide snapshot for the landing dashboard")
public record DashboardSummaryResponse(
        long totalEmployees,
        long activeProjects,
        long pendingTasks,
        long completedTasks,
        long openBugs,
        long closedBugs,
        long upcomingDeadlines,
        long todaysMeetings,
        long overloadedEmployees) {
}
