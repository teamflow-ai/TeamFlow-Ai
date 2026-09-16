package com.teamflow.ai.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "A manager-facing alert surfaced by the workload engine")
public record AlertResponse(
        AlertType type,
        Severity severity,
        String message,
        UUID employeeId,
        UUID taskId,
        UUID projectId) {

    public enum AlertType {
        OVERLOADED_EMPLOYEE,
        TOO_MANY_HIGH_PRIORITY_TASKS,
        OVERDUE_TASK,
        DEADLINE_APPROACHING,
        SPRINT_IMBALANCE
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}
