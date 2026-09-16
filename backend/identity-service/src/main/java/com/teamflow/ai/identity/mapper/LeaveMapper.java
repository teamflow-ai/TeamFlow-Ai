package com.teamflow.ai.identity.mapper;

import com.teamflow.ai.identity.dto.response.LeaveResponse;
import com.teamflow.ai.identity.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveResponse toResponse(LeaveRequest leaveRequest) {
        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .employeeName(leaveRequest.getEmployee().getFullName())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .totalDays(leaveRequest.getTotalDays())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .managerApproverId(leaveRequest.getManagerApproverId())
                .managerApprovedAt(leaveRequest.getManagerApprovedAt())
                .hrApproverId(leaveRequest.getHrApproverId())
                .hrApprovedAt(leaveRequest.getHrApprovedAt())
                .decisionComment(leaveRequest.getDecisionComment())
                .build();
    }
}
