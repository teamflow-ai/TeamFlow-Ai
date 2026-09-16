package com.teamflow.ai.project.approval.repository;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, UUID>, JpaSpecificationExecutor<ApprovalRequest> {

    Optional<ApprovalRequest> findByIdAndDeletedFalse(UUID id);

    boolean existsByReferenceEntityIdAndApprovalTypeAndStatusIn(
            UUID referenceEntityId, ApprovalType approvalType, List<ApprovalStatus> statuses);
}
