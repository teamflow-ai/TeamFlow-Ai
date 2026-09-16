package com.teamflow.ai.project.approval.handler;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.enums.MilestoneStatus;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import com.teamflow.ai.project.entity.Milestone;
import com.teamflow.ai.project.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Depends on {@link MilestoneRepository} directly rather than
 * {@code MilestoneService}, for the same circular-dependency reason documented
 * on {@link ProjectClosureApprovalHandler}: {@code MilestoneServiceImpl} is what
 * calls {@code ApprovalService} to open this request in the first place.
 */
@Component
@RequiredArgsConstructor
public class MilestoneApprovalHandler implements ApprovalOutcomeHandler {

    private final MilestoneRepository milestoneRepository;

    @Override
    public ApprovalType supportedType() {
        return ApprovalType.MILESTONE;
    }

    @Override
    public void onDecision(ApprovalRequest approvalRequest, UUID actorId) {
        Milestone milestone = milestoneRepository.findByIdAndDeletedFalse(approvalRequest.getReferenceEntityId())
                .orElseThrow(() -> ResourceNotFoundException.of("Milestone", approvalRequest.getReferenceEntityId()));

        MilestoneStatus newStatus = switch (approvalRequest.getStatus()) {
            case APPROVED -> MilestoneStatus.APPROVED;
            case REJECTED -> MilestoneStatus.REJECTED;
            default -> MilestoneStatus.IN_PROGRESS; // RETURNED_FOR_CHANGES: back to work
        };
        milestone.setStatus(newStatus);
        milestoneRepository.save(milestone);
    }
}
