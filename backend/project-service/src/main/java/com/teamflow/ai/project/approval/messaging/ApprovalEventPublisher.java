package com.teamflow.ai.project.approval.messaging;

import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.event.ApprovalEvent;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mirrors the pattern of {@code ProjectEventPublisher}: publish failures are
 * logged, never thrown, so a messaging outage cannot fail the approval decision
 * itself.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(ApprovalRequest approvalRequest, ApprovalStatus previousStatus, UUID actorId) {
        String routingKey = switch (approvalRequest.getStatus()) {
            case APPROVED -> MessagingConstants.APPROVAL_APPROVED;
            case REJECTED -> MessagingConstants.APPROVAL_REJECTED;
            case RETURNED_FOR_CHANGES -> MessagingConstants.APPROVAL_RETURNED;
            case CANCELLED -> MessagingConstants.APPROVAL_CANCELLED;
            case PENDING, UNDER_REVIEW -> MessagingConstants.APPROVAL_REQUESTED;
        };
        try {
            ApprovalEvent event = ApprovalEvent.of(routingKey, approvalRequest.getId(),
                    approvalRequest.getApprovalType(), approvalRequest.getReferenceEntityId(),
                    approvalRequest.getProjectId(), approvalRequest.getRequestedBy(), 
                    approvalRequest.getApproverId(), actorId,
                    previousStatus, approvalRequest.getStatus(), approvalRequest.getRemarks());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for approval request {}: {}",
                    routingKey, approvalRequest.getId(), ex.getMessage());
        }
    }
}
