package com.teamflow.ai.project.approval.event;

import java.util.UUID;

/**
 * In-process Spring event (not a RabbitMQ {@code DomainEvent}) published by
 * {@code TaskServiceImpl} the moment a task's status becomes {@code IN_REVIEW}.
 *
 * <p>{@code TaskApprovalRequestListener} reacts to this by opening a
 * {@code TASK_COMPLETION} approval request. Using a local event here — rather
 * than TaskServiceImpl calling {@code ApprovalService} directly — breaks what
 * would otherwise be a circular Spring bean dependency, since the
 * {@code TASK_COMPLETION} outcome handler calls back into {@code TaskService}.
 */
public record TaskSubmittedForReviewEvent(UUID taskId, UUID projectId, UUID approverId, UUID submittedBy) {
}
