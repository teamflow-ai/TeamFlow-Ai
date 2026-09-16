package com.teamflow.ai.project.approval.dto;

import com.teamflow.ai.common.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "A decision on a pending approval request")
public record ApprovalDecisionRequest(

        @NotNull(message = "Decision is required")
        @Schema(description = "Must be APPROVED, REJECTED or RETURNED_FOR_CHANGES")
        ApprovalStatus decision,

        @Size(max = 1000)
        String remarks) {

    @AssertTrue(message = "Decision must be APPROVED, REJECTED or RETURNED_FOR_CHANGES")
    public boolean isValidDecision() {
        return decision == null
                || decision == ApprovalStatus.APPROVED
                || decision == ApprovalStatus.REJECTED
                || decision == ApprovalStatus.RETURNED_FOR_CHANGES;
    }
}
