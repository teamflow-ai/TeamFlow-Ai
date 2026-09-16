package com.teamflow.ai.project.approval.listener;

import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.project.approval.event.TaskSubmittedForReviewEvent;
import com.teamflow.ai.project.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges {@link TaskSubmittedForReviewEvent} (published by {@code TaskServiceImpl})
 * to the generic Approval Workflow Engine, implementing the product brief's "Task
 * Completion Approval" flow without TaskServiceImpl depending on ApprovalService
 * directly (see the javadoc on {@code TaskSubmittedForReviewEvent}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskApprovalRequestListener {

    private final ApprovalService approvalService;

    /**
     * Runs after the status-change transaction commits, so the approval request is
     * only opened once the task really is IN_REVIEW in the database.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskSubmittedForReview(TaskSubmittedForReviewEvent event) {
        try {
            approvalService.request(ApprovalType.TASK_COMPLETION, event.taskId(), event.projectId(),
                    event.submittedBy(), event.approverId(), "Task submitted for completion review");
        } catch (Exception ex) {
            // A duplicate/likely-already-pending approval must never fail the task update that
            // already committed; log and move on, same fail-soft stance as the event publishers.
            log.warn("Could not open a TASK_COMPLETION approval for task {}: {}", event.taskId(), ex.getMessage());
        }
    }
}
