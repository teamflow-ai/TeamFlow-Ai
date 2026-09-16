package com.teamflow.ai.identity.dto.response;

import com.teamflow.ai.common.enums.LeaveStatus;
import com.teamflow.ai.identity.entity.LeaveRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Schema(description = "Leave request")
public record LeaveResponse(

        UUID id,
        UUID employeeId,
        String employeeName,
        LeaveRequest.LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        String reason,
        LeaveStatus status,
        UUID managerApproverId,
        Instant managerApprovedAt,
        UUID hrApproverId,
        Instant hrApprovedAt,
        String decisionComment) {
}
