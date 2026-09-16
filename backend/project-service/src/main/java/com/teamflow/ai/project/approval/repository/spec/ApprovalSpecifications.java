package com.teamflow.ai.project.approval.repository.spec;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ApprovalSpecifications {

    private ApprovalSpecifications() {
    }

    public static Specification<ApprovalRequest> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ApprovalRequest> hasType(ApprovalType approvalType) {
        if (approvalType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("approvalType"), approvalType);
    }

    public static Specification<ApprovalRequest> hasStatus(ApprovalStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ApprovalRequest> forApprover(UUID approverId) {
        if (approverId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("approverId"), approverId);
    }

    public static Specification<ApprovalRequest> inProject(UUID projectId) {
        if (projectId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("projectId"), projectId);
    }
}
