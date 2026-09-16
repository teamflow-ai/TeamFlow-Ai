package com.teamflow.ai.ai.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AuditHistoryResponse(
        String id,
        String entityType,
        String entityId,
        String approvalRequestId,
        String projectId,
        String requestedBy,
        String performedBy,
        String previousStatus,
        String newStatus,
        String remarks,
        Instant timestamp) {
}
