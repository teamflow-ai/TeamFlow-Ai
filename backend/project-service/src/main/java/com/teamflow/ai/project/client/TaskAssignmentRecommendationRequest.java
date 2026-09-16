package com.teamflow.ai.project.client;

import com.teamflow.ai.common.enums.Priority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** What project-service sends ai-service to ask "who should get this task?" */
public record TaskAssignmentRecommendationRequest(
        UUID taskId,
        UUID projectId,
        Set<String> requiredSkills,
        Priority priority,
        BigDecimal estimatedHours,
        LocalDate dueDate) {
}
