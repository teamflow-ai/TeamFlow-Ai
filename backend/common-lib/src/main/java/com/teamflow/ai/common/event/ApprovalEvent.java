package com.teamflow.ai.common.event;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted on every state transition of an {@code ApprovalRequest}, regardless of
 * approval type.
 *
 * <p>ai-service consumes this once to serve two purposes that the product brief
 * asks for separately but which map to the same underlying fact — a status
 * transition on an approval: the queryable "approval history" and the generic
 * "audit log" (see {@code AuditHistory} in ai-service). Keeping one event and one
 * Mongo collection for both avoids maintaining two near-identical records of the
 * same transition.
 */
public record ApprovalEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID approvalRequestId,
        ApprovalType approvalType,
        UUID referenceEntityId,
        UUID projectId,
        UUID requestedBy,
        UUID approverId,
        UUID actorId,
        ApprovalStatus previousStatus,
        ApprovalStatus newStatus,
        String remarks) implements DomainEvent {

    public static ApprovalEvent of(String routingKey, UUID approvalRequestId, ApprovalType approvalType,
                                    UUID referenceEntityId, UUID projectId, UUID requestedBy, UUID approverId, UUID actorId,
                                    ApprovalStatus previousStatus, ApprovalStatus newStatus, String remarks) {
        return new ApprovalEvent(UUID.randomUUID(), Instant.now(), routingKey, approvalRequestId, approvalType,
                referenceEntityId, projectId, requestedBy, approverId, actorId, previousStatus, newStatus, remarks);
    }
}
