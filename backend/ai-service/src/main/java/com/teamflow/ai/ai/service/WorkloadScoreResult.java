package com.teamflow.ai.ai.service;

import com.teamflow.ai.common.enums.BurnoutRisk;

import java.util.UUID;

/**
 * The scored, explainable output of {@link WorkloadScoringService} for one
 * employee.
 *
 * <p>{@code factors} are kept as raw numbers rather than pre-formatted strings so
 * that both the recommendation engine (needs a ranking) and the dashboard (needs a
 * table) can consume the same computation without recomputing it twice.
 */
public record WorkloadScoreResult(
        UUID employeeId,
        String employeeName,
        double score,
        BurnoutRisk band,
        int activeTaskCount,
        int overdueTaskCount,
        double remainingEstimatedHours,
        double totalAllocatedHours,
        int openBugCount,
        boolean onLeaveToday) {
}
