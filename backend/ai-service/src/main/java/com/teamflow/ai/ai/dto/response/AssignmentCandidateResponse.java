package com.teamflow.ai.ai.dto.response;

import com.teamflow.ai.common.enums.BurnoutRisk;
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
        BurnoutRisk riskBand,
        int activeTaskCount,
        int overdueTaskCount,
        double remainingEstimatedHours,
        int openBugCount,
        boolean onLeaveToday,
        double utilizationPercent,
        
        // AI Recommendation Context
        boolean isRecommended,
        List<String> recommendationReasons
) {}
