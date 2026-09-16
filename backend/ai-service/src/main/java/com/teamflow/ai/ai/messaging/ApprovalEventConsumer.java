package com.teamflow.ai.ai.messaging;

import com.teamflow.ai.ai.document.AuditHistory;
import com.teamflow.ai.ai.repository.AuditHistoryRepository;
import com.teamflow.ai.ai.service.NotificationService;
import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.event.ApprovalEvent;
import com.teamflow.ai.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes every {@link ApprovalEvent} regardless of {@code ApprovalType}: writes
 * one {@link AuditHistory} row per transition, and notifies whoever needs to act
 * next — the approver when a request opens, the requester once it's decided —
 * by reusing {@link NotificationService#receive} rather than duplicating its
 * persist-plus-email logic here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventConsumer {

    private final AuditHistoryRepository auditHistoryRepository;
    private final NotificationService notificationService;

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_APPROVAL_EVENTS)
    public void onApprovalEvent(ApprovalEvent event) {
        AuditHistory audit = new AuditHistory();
        audit.setId(event.eventId().toString());
        audit.setEntityType(event.approvalType().name());
        audit.setEntityId(event.referenceEntityId().toString());
        audit.setApprovalRequestId(event.approvalRequestId().toString());
        audit.setProjectId(event.projectId() != null ? event.projectId().toString() : null);
        audit.setRequestedBy(event.requestedBy().toString());
        audit.setPerformedBy(event.actorId() != null ? event.actorId().toString() : null);
        audit.setPreviousStatus(event.previousStatus() != null ? event.previousStatus().name() : null);
        audit.setNewStatus(event.newStatus().name());
        audit.setRemarks(event.remarks());
        audit.setTimestamp(event.occurredAt());
        auditHistoryRepository.save(audit);
        log.debug("Recorded audit history for approval {} ({} -> {})",
                event.approvalRequestId(), event.previousStatus(), event.newStatus());

        notify(event);
    }

    private void notify(ApprovalEvent event) {
        String typeLabel = event.approvalType().name().replace('_', ' ').toLowerCase();

        if (event.newStatus() == ApprovalStatus.PENDING) {
            // Newly opened request: tell the approver, if one is already assigned. Requests with
            // no pre-assigned approver (e.g. project closure, open to any admin) rely on the
            // approver's own inbox — GET /api/v1/approvals?status=PENDING — rather than a push.
            send(event.approverId(), event.approvalRequestId(), "Approval requested",
                    "A " + typeLabel + " approval is waiting for your decision", "APPROVAL_REQUESTED");
            return;
        }

        String title = switch (event.newStatus()) {
            case APPROVED -> "Approval granted";
            case REJECTED -> "Approval rejected";
            case RETURNED_FOR_CHANGES -> "Sent back for changes";
            default -> "Approval updated";
        };
        String body = "Your " + typeLabel + " request was " + event.newStatus().name().toLowerCase().replace('_', ' ')
                + (event.remarks() != null && !event.remarks().isBlank() ? ": " + event.remarks() : "");
        send(event.requestedBy(), event.approvalRequestId(), title, body, "APPROVAL_" + event.newStatus().name());
    }

    private void send(java.util.UUID recipient, java.util.UUID approvalRequestId, String title, String body,
                       String category) {
        if (recipient == null) {
            return;
        }
        notificationService.receive(
                NotificationEvent.of(recipient, title, body, category, "/approvals/" + approvalRequestId));
    }
}
