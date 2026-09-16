package com.teamflow.ai.project.client;

import java.util.List;
import java.util.UUID;

/**
 * One ranked candidate from ai-service's workload scorer.
 *
 * <p>{@code reasons} is deliberately plain text rather than a coded enum: the
 * whole point of the recommendation engine is that a manager can read why an
 * employee was suggested, not just trust a black-box score.
 */
public record TaskAssignmentRecommendation(
        UUID employeeId,
        String employeeName,
        double workloadScore,
        List<String> reasons) {
}
