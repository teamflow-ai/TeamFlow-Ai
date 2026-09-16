package com.teamflow.ai.ai.dto.request;

import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * What project-service asks when a manager wants smart-assignment candidates.
 *
 * <p>Field names deliberately mirror project-service's
 * {@code TaskAssignmentRecommendationRequest} exactly: the two services aren't
 * sharing a Java type (they're independently deployable), but they do share a JSON
 * shape, and Jackson maps by field name.
 */
@Schema(description = "Request for ranked task-assignment candidates")
public record TaskAssignmentRequest(
        UUID taskId,
        UUID projectId,
        Set<String> requiredSkills,
        Priority priority,
        BigDecimal estimatedHours,
        LocalDate dueDate) {
}
