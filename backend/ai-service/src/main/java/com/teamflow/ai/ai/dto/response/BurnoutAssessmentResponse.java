package com.teamflow.ai.ai.dto.response;

import com.teamflow.ai.common.enums.BurnoutRisk;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record BurnoutAssessmentResponse(
        UUID employeeId,
        String employeeName,
        BurnoutRisk risk,
        double workloadScore,
        int activeTaskCount,
        int overdueTaskCount,
        double remainingEstimatedHours,
        List<String> suggestions,
        String reasoning) {
}
