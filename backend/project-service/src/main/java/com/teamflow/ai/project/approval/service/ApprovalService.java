package com.teamflow.ai.project.approval.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.project.approval.dto.ApprovalDecisionRequest;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApprovalService {

    /**
     * Opens a new approval request. Called by the owning module's service (e.g.
     * {@code MilestoneServiceImpl}) or by a listener reacting to a domain event
     * (e.g. task submitted for review) — never directly by a controller, so that
     * only a legitimate business action can create one.
     */
    ApprovalResponse request(ApprovalType approvalType, UUID referenceEntityId, UUID projectId,
                              UUID requestedBy, UUID approverId, String remarks);

    /** Records a decision and, if a handler is registered for this type, applies its effect. */
    ApprovalResponse decide(UUID approvalId, UUID actorId, ApprovalDecisionRequest request);

    ApprovalResponse get(UUID approvalId);

    PageResponse<ApprovalResponse> search(ApprovalType approvalType, ApprovalStatus status,
                                          UUID approverId, UUID projectId, Pageable pageable);

    boolean hasOpenApproval(UUID referenceEntityId, ApprovalType approvalType);
}
