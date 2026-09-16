package com.teamflow.ai.ai.dto.response;

import com.teamflow.ai.common.enums.BurnoutRisk;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "One employee's current workload standing")
public record EmployeeWorkloadResponse(
        UUID employeeId,
        String employeeName,
        double workloadScore,
        BurnoutRisk band,
        int activeTaskCount,
        int overdueTaskCount,
        double remainingEstimatedHours,
        int openBugCount,
        boolean onLeaveToday) {
}
