package com.teamflow.ai.project.approval.service.impl;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.approval.dto.ApprovalDecisionRequest;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import com.teamflow.ai.project.approval.handler.ApprovalOutcomeHandler;
import com.teamflow.ai.project.approval.mapper.ApprovalMapper;
import com.teamflow.ai.project.approval.messaging.ApprovalEventPublisher;
import com.teamflow.ai.project.approval.repository.ApprovalRequestRepository;
import com.teamflow.ai.project.approval.repository.spec.ApprovalSpecifications;
import com.teamflow.ai.project.approval.service.ApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Approval Workflow Engine.
 *
 * <p>Deliberately knows nothing about milestones, tasks, projects or clients —
 * see {@link ApprovalOutcomeHandler}. Every {@code ApprovalType} in the enum is
 * legal to request even without a registered handler (the row is still tracked,
 * audited and notified on); a handler is only needed when a decision must also
 * change some other entity's state, which today is every type except
 * {@link ApprovalType#CLIENT_APPROVAL} — that one is deliberately recorded here
 * only, since the deliverable's own state (sent to client) does not change based
 * on this system's data.
 */
@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final List<ApprovalStatus> OPEN_STATUSES =
            List.of(ApprovalStatus.PENDING, ApprovalStatus.UNDER_REVIEW);

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalMapper approvalMapper;
    private final ApprovalEventPublisher eventPublisher;
    private final Map<ApprovalType, ApprovalOutcomeHandler> handlersByType;

    public ApprovalServiceImpl(ApprovalRequestRepository approvalRequestRepository,
                                ApprovalMapper approvalMapper,
                                ApprovalEventPublisher eventPublisher,
                                List<ApprovalOutcomeHandler> handlers) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalMapper = approvalMapper;
        this.eventPublisher = eventPublisher;
        this.handlersByType = handlers.stream()
                .collect(Collectors.toMap(ApprovalOutcomeHandler::supportedType, Function.identity()));
    }

    @Override
    @Transactional
    public ApprovalResponse request(ApprovalType approvalType, UUID referenceEntityId, UUID projectId,
                                     UUID requestedBy, UUID approverId, String remarks) {
        if (approvalRequestRepository.existsByReferenceEntityIdAndApprovalTypeAndStatusIn(
                referenceEntityId, approvalType, OPEN_STATUSES)) {
            throw new BusinessException(
                    "A %s approval is already pending for this item".formatted(approvalType));
        }

        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setApprovalType(approvalType);
        approvalRequest.setReferenceEntityId(referenceEntityId);
        approvalRequest.setProjectId(projectId);
        approvalRequest.setRequestedBy(requestedBy);
        approvalRequest.setApproverId(approverId);
        approvalRequest.setRemarks(remarks);
        approvalRequest.setStatus(ApprovalStatus.PENDING);
        approvalRequest.setRequestedDate(Instant.now());

        ApprovalRequest saved = approvalRequestRepository.save(approvalRequest);
        log.info("Approval {} requested for {} {} by {}", saved.getId(), approvalType, referenceEntityId, requestedBy);
        eventPublisher.publish(saved, null, requestedBy);
        return approvalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalResponse decide(UUID approvalId, UUID actorId, ApprovalDecisionRequest request) {
        ApprovalRequest approvalRequest = findOrThrow(approvalId);
        ApprovalStatus previousStatus = approvalRequest.getStatus();

        if (!previousStatus.isDecidable()) {
            throw new BusinessException(
                    "Approval %s is already %s and cannot be decided again".formatted(approvalId, previousStatus));
        }
        requireApproverPermission(approvalRequest.getApprovalType());

        if (actorId.equals(approvalRequest.getRequestedBy())) {
            throw new BusinessException("You cannot approve your own request (four-eyes principle)");
        }

        approvalRequest.setStatus(request.decision());
        approvalRequest.setRemarks(request.remarks());
        approvalRequest.setApproverId(actorId);
        Instant now = Instant.now();
        if (request.decision() == ApprovalStatus.APPROVED) {
            approvalRequest.setApprovedDate(now);
        } else if (request.decision() == ApprovalStatus.REJECTED) {
            approvalRequest.setRejectedDate(now);
        }

        ApprovalRequest saved = approvalRequestRepository.save(approvalRequest);
        log.info("Approval {} ({}) decided: {} -> {} by {}",
                saved.getId(), saved.getApprovalType(), previousStatus, saved.getStatus(), actorId);

        ApprovalOutcomeHandler handler = handlersByType.get(saved.getApprovalType());
        if (handler != null) {
            handler.onDecision(saved, actorId);
        } else {
            log.debug("No ApprovalOutcomeHandler registered for {}; recording the decision only", saved.getApprovalType());
        }

        eventPublisher.publish(saved, previousStatus, actorId);
        return approvalMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalResponse get(UUID approvalId) {
        return approvalMapper.toResponse(findOrThrow(approvalId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApprovalResponse> search(ApprovalType approvalType, ApprovalStatus status,
                                                 UUID approverId, UUID projectId, Pageable pageable) {
        Specification<ApprovalRequest> spec = Specification.allOf(
                Stream.of(
                        ApprovalSpecifications.notDeleted(),
                        ApprovalSpecifications.hasType(approvalType),
                        ApprovalSpecifications.hasStatus(status),
                        ApprovalSpecifications.forApprover(approverId),
                        ApprovalSpecifications.inProject(projectId)
                ).filter(Objects::nonNull).collect(Collectors.toList())
        );
        Page<ApprovalRequest> page = approvalRequestRepository.findAll(spec, pageable);
        return PageResponse.from(page, approvalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenApproval(UUID referenceEntityId, ApprovalType approvalType) {
        return approvalRequestRepository.existsByReferenceEntityIdAndApprovalTypeAndStatusIn(
                referenceEntityId, approvalType, OPEN_STATUSES);
    }

    private ApprovalRequest findOrThrow(UUID id) {
        return approvalRequestRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ApprovalRequest", id));
    }

    /**
     * The controller's {@code @PreAuthorize} only checks that the caller holds
     * <em>some</em> approver permission; here we check the one specific to this
     * request's actual type, since a project manager holding
     * {@code APPROVE_TASK_COMPLETION} must not be able to decide a
     * {@code PROJECT_CLOSURE} request meant for an admin.
     */
    private void requireApproverPermission(ApprovalType approvalType) {
        String required = switch (approvalType) {
            case MILESTONE -> PermissionNames.APPROVE_MILESTONE;
            case TASK_COMPLETION -> PermissionNames.APPROVE_TASK_COMPLETION;
            case PROJECT_CLOSURE -> PermissionNames.APPROVE_PROJECT_CLOSURE;
            case CLIENT_APPROVAL -> PermissionNames.MANAGE_CLIENT_APPROVAL;
        };
        if (!SecurityUtils.requireCurrentUser().hasPermission(required)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Deciding a %s approval requires the %s permission".formatted(approvalType, required));
        }
    }
}
