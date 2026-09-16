package com.teamflow.ai.project.approval.dto;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ApprovalResponse(
        UUID id,
        ApprovalType approvalType,
        UUID referenceEntityId,
        UUID projectId,
        UUID requestedBy,
        UUID approverId,
        ApprovalStatus status,
        String remarks,
        Instant requestedDate,
        Instant approvedDate,
        Instant rejectedDate) {
}
