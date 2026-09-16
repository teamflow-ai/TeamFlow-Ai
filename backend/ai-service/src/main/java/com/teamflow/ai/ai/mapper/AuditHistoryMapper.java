package com.teamflow.ai.ai.mapper;

import com.teamflow.ai.ai.document.AuditHistory;
import com.teamflow.ai.ai.dto.response.AuditHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class AuditHistoryMapper {

    public AuditHistoryResponse toResponse(AuditHistory audit) {
        return AuditHistoryResponse.builder()
                .id(audit.getId())
                .entityType(audit.getEntityType())
                .entityId(audit.getEntityId())
                .approvalRequestId(audit.getApprovalRequestId())
                .projectId(audit.getProjectId())
                .requestedBy(audit.getRequestedBy())
                .performedBy(audit.getPerformedBy())
                .previousStatus(audit.getPreviousStatus())
                .newStatus(audit.getNewStatus())
                .remarks(audit.getRemarks())
                .timestamp(audit.getTimestamp())
                .build();
    }
}
