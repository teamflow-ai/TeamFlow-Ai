package com.teamflow.ai.project.approval.handler;

import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.project.approval.entity.ApprovalRequest;
import com.teamflow.ai.project.dto.request.UpdateTaskStatusRequest;
import com.teamflow.ai.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * "Task Completion Approval": the manager either confirms completion (task moves
 * to {@code DONE}) or sends it back with comments (task returns to
 * {@code IN_PROGRESS}, remarks already stored on the {@code ApprovalRequest}
 * itself for the employee to read).
 *
 * <p>Reuses {@link TaskService#updateStatus} rather than writing to
 * {@code TaskRepository} directly, so the existing transition validation, status
 * history recording and event publishing all still run — "reuse existing
 * business logic whenever possible."
 */
@Component
@RequiredArgsConstructor
public class TaskCompletionApprovalHandler implements ApprovalOutcomeHandler {

    private final TaskService taskService;

    @Override
    public ApprovalType supportedType() {
        return ApprovalType.TASK_COMPLETION;
    }

    @Override
    public void onDecision(ApprovalRequest approvalRequest, UUID actorId) {
        ApprovalStatus decision = approvalRequest.getStatus();
        TaskStatus target = decision == ApprovalStatus.APPROVED ? TaskStatus.DONE : TaskStatus.IN_PROGRESS;
        taskService.updateStatus(approvalRequest.getReferenceEntityId(), new UpdateTaskStatusRequest(target), actorId);
    }
}
