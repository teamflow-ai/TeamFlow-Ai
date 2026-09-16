package com.teamflow.ai.project.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Detailed assignment candidate profile with workload metrics")
public record AssignmentCandidateResponse(
        UUID employeeId,
        String employeeName,
        Set<String> skills,
        int weeklyCapacityHours,
        
        // Workload Metrics
        double workloadScore,
        String riskBand,
        int activeTaskCount,
        int overdueTaskCount,
        double remainingEstimatedHours,
        double totalAllocatedHours,
        int openBugCount,
        boolean onLeaveToday,
        double utilizationPercent,
        
        // AI Recommendation Context
        boolean isRecommended,
        List<String> recommendationReasons
) {}
