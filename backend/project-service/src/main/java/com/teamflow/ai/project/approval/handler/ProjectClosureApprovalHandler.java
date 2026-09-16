package com.teamflow.ai.project.approval.handler;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * "Project Completion Approval": on APPROVED, the project moves to
 * {@code COMPLETED} and reports/analytics pick that up on their next read since
 * they query current project state directly, no separate "archive" step needed.
 *
 * <p>Depends on {@link ProjectRepository} directly rather than
 * {@code ProjectService}, since {@code ProjectServiceImpl} depends on
 * {@code ApprovalService} to open the closure request in the first place —
 * going through {@code ProjectService} here would recreate the same circular
 * bean graph problem documented on {@code TaskServiceImpl}.
 */
@Component
@RequiredArgsConstructor
public class ProjectClosureApprovalHandler implements ApprovalOutcomeHandler {

    private final ProjectRepository projectRepository;
    private final ProjectEventPublisher eventPublisher;

    @Override
    public ApprovalType supportedType() {
        return ApprovalType.PROJECT_CLOSURE;
    }

    @Override
    public void onDecision(ApprovalRequest approvalRequest, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedFalse(approvalRequest.getReferenceEntityId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", approvalRequest.getReferenceEntityId()));

        if (approvalRequest.getStatus() == ApprovalStatus.APPROVED) {
            project.setStatus(ProjectStatus.COMPLETED);
            Project saved = projectRepository.save(project);
            eventPublisher.projectCompleted(saved);
            eventPublisher.notify(saved.getManagerId(), "Project closure approved",
                    "\"%s\" has been closed".formatted(saved.getName()), "PROJECT_COMPLETED",
                    "/projects/" + saved.getId());
        } else {
            eventPublisher.notify(project.getManagerId(), "Project closure not approved",
                    "The closure request for \"%s\" was not approved: %s"
                            .formatted(project.getName(), approvalRequest.getRemarks()),
                    "PROJECT_UPDATED", "/projects/" + project.getId());
        }
    }
}
