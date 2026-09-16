package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Approve or reject a leave request")
public record LeaveDecisionRequest(

        @NotNull(message = "approve is required")
        boolean approve,

        @Size(max = 500)
        String comment) {
}
