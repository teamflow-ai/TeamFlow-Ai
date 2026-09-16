package com.teamflow.ai.identity.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DepartmentReportResponse(
        UUID departmentId,
        String departmentName,
        long totalEmployees,
        long activeEmployees,
        long inactiveEmployees,
        long pendingLeaveRequests) {
}
