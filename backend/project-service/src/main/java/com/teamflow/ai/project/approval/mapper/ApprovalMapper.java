package com.teamflow.ai.project.approval.mapper;

import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import org.springframework.stereotype.Component;

@Component
public class ApprovalMapper {

    public ApprovalResponse toResponse(ApprovalRequest approvalRequest) {
        return ApprovalResponse.builder()
                .id(approvalRequest.getId())
                .approvalType(approvalRequest.getApprovalType())
                .referenceEntityId(approvalRequest.getReferenceEntityId())
                .projectId(approvalRequest.getProjectId())
                .requestedBy(approvalRequest.getRequestedBy())
                .approverId(approvalRequest.getApproverId())
                .status(approvalRequest.getStatus())
                .remarks(approvalRequest.getRemarks())
                .requestedDate(approvalRequest.getRequestedDate())
                .approvedDate(approvalRequest.getApprovedDate())
                .rejectedDate(approvalRequest.getRejectedDate())
                .build();
    }
}
