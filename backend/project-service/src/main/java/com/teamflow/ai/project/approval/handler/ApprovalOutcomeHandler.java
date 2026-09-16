package com.teamflow.ai.project.approval.handler;

import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;

import java.util.UUID;

/**
 * Applies the business-specific side effect of a decision on one {@link ApprovalType}.
 *
 * <p>This is what keeps the Approval Workflow Engine generic and reusable: the
 * engine itself ({@code ApprovalService}) only manages the {@code ApprovalRequest}
 * row's lifecycle, publishes the audit event and fires notifications. It knows
 * nothing about milestones, tasks or projects. Adding a new approval type is
 * adding one {@link ApprovalType} constant and one class implementing this
 * interface — no change to the engine.
 *
 * <p>Every implementation is a Spring bean; {@code ApprovalServiceImpl} collects
 * all of them and dispatches by {@link #supportedType()}.
 */
public interface ApprovalOutcomeHandler {

    ApprovalType supportedType();

    /**
     * Called after the {@code ApprovalRequest} row has already been persisted in
     * its new status. Implementations apply whatever effect that decision has on
     * the underlying entity (e.g. flipping a {@code Milestone} to {@code APPROVED},
     * or a {@code Task} back to {@code IN_PROGRESS}).
     *
     * @param approvalRequest the request, already saved with its new status
     * @param actorId         the employee who made the decision
     */
    void onDecision(ApprovalRequest approvalRequest, UUID actorId);
}
